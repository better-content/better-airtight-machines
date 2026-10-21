#!/usr/bin/env python3
"""Nonshipping native-fixture completion from exactly two approved tracked defaults."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import uuid
import zipfile

APPROVED = {
    'config/adpother-common.toml': 'd255babc81f6729ce3442522c76cab2a05f960b496f292fdc0371af69745f514',
    'config/forgeendertech-common.toml': 'e3c7fbcf2865785d6dd5ec72b3db1abae3e7ba6376bd3aecc0472586ed21b6ad',
}


class FixtureError(RuntimeError):
    pass


def no_symlink_ancestors(path):
    if any(p.is_symlink() for p in [path.absolute(), *path.absolute().parents]):
        raise FixtureError('Symlinked fixture authority/snapshot/destination')


def require_native_receipt(run, base):
    for path in [run, base]:
        no_symlink_ancestors(path)
    if run.resolve().parent != base.resolve() or base.name != 'gametest' or base.parent.name != 'build':
        raise FixtureError('Refusing nonconfigured native base')
    try:
        if str(uuid.UUID(run.name)) != run.name:
            raise ValueError('noncanonical token')
    except ValueError as failure:
        raise FixtureError('Native UUID is not canonical') from failure
    marker = run / 'source-fixture.json'
    if not marker.is_file() or marker.is_symlink():
        raise FixtureError('Missing actual owning source-fixture receipt')
    data = json.loads(marker.read_text())
    if (data.get('working_directory') != str(run.resolve()) or data.get('expected_native_tests') != 3
            or data.get('mode') != 'authored_gametests'
            or not all(data.get(key) is True for key in ['old_world_absent', 'old_latest_log_absent', 'old_debug_log_absent'])
            or any((run / name).exists() for name in ['world', 'logs/latest.log', 'logs/debug.log', 'native-default-config-inputs.json'])):
        raise FixtureError('Stale/foreign/incomplete owning native receipt or prepared world/log')


def digest(data):
    return hashlib.sha256(data).hexdigest()


def git(root, *args):
    result = subprocess.run(['git', '-C', str(root), *args], capture_output=True)
    if result.returncode:
        raise FixtureError('Missing/untracked authoritative input or unavailable Git HEAD')
    return result.stdout


def authority(root):
    no_symlink_ancestors(root)
    result = {}
    for name, expected in APPROVED.items():
        path = root / name
        no_symlink_ancestors(path)
        if not path.is_file() or path.is_symlink():
            raise FixtureError('Missing or symlinked authoritative input: ' + name)
        git(root, 'ls-files', '--error-unmatch', name)
        blob = git(root, 'show', 'HEAD:' + name)
        if digest(blob) != expected or path.read_bytes() != blob:
            raise FixtureError('Authoritative tracked HEAD/working bytes mismatch: ' + name)
        result[name] = {'sha256': expected, 'git_blob': git(root, 'rev-parse', 'HEAD:' + name).decode().strip()}
    return {'root_head': git(root, 'rev-parse', 'HEAD').decode().strip(), 'files': result}


def create_snapshot(root, destination):
    no_symlink_ancestors(destination)
    origin = authority(root)
    destination.mkdir(parents=True, exist_ok=False)
    for name in APPROVED:
        target = destination / name
        target.parent.mkdir(parents=True, exist_ok=True)
        with target.open('xb') as stream:
            stream.write((root / name).read_bytes())
            stream.flush(); os.fsync(stream.fileno())
        if digest(target.read_bytes()) != APPROVED[name]:
            raise FixtureError('Authoritative source changed during snapshot creation')
        target.chmod(0o444)
    with (destination / 'origin.json').open('x') as stream:
        json.dump(origin, stream, indent=2)
        stream.write('\n'); stream.flush(); os.fsync(stream.fileno())
    (destination / 'origin.json').chmod(0o444)
    if authority(root)['files'] != origin['files']:
        raise FixtureError('Scoped authority changed during snapshot creation')


def validate_snapshot(root, snapshot):
    current = authority(root)
    names = {str(p.relative_to(snapshot)) for p in snapshot.rglob('*') if p.is_file()}
    if names != set(APPROVED) | {'origin.json'} or any(p.is_symlink() for p in snapshot.rglob('*')):
        raise FixtureError('Snapshot must contain exactly the two allowlisted inputs and origin.json')
    origin = json.loads((snapshot / 'origin.json').read_text())
    if set(origin['files']) != set(APPROVED) or not re.fullmatch(r'(?:[0-9a-f]{40}|[0-9a-f]{64})', origin['root_head']):
        raise FixtureError('Snapshot origin allowlist/immutable HEAD mismatch')
    for name, expected in APPROVED.items():
        if digest((snapshot / name).read_bytes()) != expected or origin['files'][name] != current['files'][name]:
            raise FixtureError('Immutable snapshot/provenance mismatch: ' + name)
        if digest(git(root, 'show', origin['root_head'] + ':' + name)) != expected:
            raise FixtureError('Snapshot original tracked HEAD mismatch: ' + name)
    return current, origin


def prepare(root, snapshot, run, base):
    for path in [root, snapshot, run, base]:
        no_symlink_ancestors(path)
    root, snapshot = root.resolve(), snapshot.resolve()
    # Validate the configured native destination before any fixture writes.
    if run.is_symlink() or run.resolve().parent != base.resolve() or base.name != 'gametest' or base.parent.name != 'build':
        raise FixtureError('Refusing nonconfigured UUID native destination')
    try:
        uuid.UUID(run.name)
    except ValueError as failure:
        raise FixtureError('Native directory is not a UUID') from failure
    current, origin = validate_snapshot(root, snapshot)
    targets = {}
    for name, expected in APPROVED.items():
        target = run / name
        if any(parent.is_symlink() for parent in [target, target.parent]):
            raise FixtureError('Symlinked native config destination')
        before = target.read_bytes() if target.exists() else None
        if before is not None and digest(before) != expected:
            raise FixtureError('Existing native input differs; bytes preserved: ' + name)
        targets[name] = (target, before)
    records = []
    for name, (target, before) in targets.items():
        target.parent.mkdir(parents=True, exist_ok=True)
        try:
            with target.open('xb') as stream:
                stream.write((snapshot / name).read_bytes())
                stream.flush(); os.fsync(stream.fileno())
        except FileExistsError:
            # Do not overwrite an existing file, even if another actor created it after preflight.
            pass
        after = target.read_bytes()
        if digest(after) != APPROVED[name] or (before is not None and after != before):
            raise FixtureError('Native config changed or mismatched; not overwritten: ' + name)
        records.append({'source': name, 'source_sha256': APPROVED[name], 'git_blob': current['files'][name]['git_blob'],
                        'destination': str(target.resolve()), 'before_sha256': digest(before) if before is not None else None,
                        'after_sha256': digest(after), 'existing_bytes_preserved': before is not None})
    receipt = {'phase': 'prepared before native launch', 'utc': datetime.now(timezone.utc).isoformat(),
               'actual_configured_run': str(run.resolve()),
               'authority': current, 'immutable_snapshot_origin': origin, 'inputs': records}
    with (run / 'native-default-config-inputs.json').open('x') as stream:
        json.dump(receipt, stream, indent=2)
        stream.write('\n'); stream.flush(); os.fsync(stream.fileno())
    if authority(root)['files'] != current['files']:
        raise FixtureError('Scoped authority changed during native preparation')
    return receipt


def check_shipping_archive(path):
    with zipfile.ZipFile(path) as archive:
        leaks = [name for name in archive.namelist() if any(part in name for part in
                 ['native-default-config', 'native_default_config', 'adpother-common.toml', 'forgeendertech-common.toml', 'origin.json'])]
    if leaks:
        raise FixtureError('Nonshipping default-input fixture leaked into archive: ' + ', '.join(leaks))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['snapshot', 'prepare', 'archives'])
    parser.add_argument('--root', type=Path)
    parser.add_argument('--snapshot', type=Path)
    parser.add_argument('--run', type=Path)
    parser.add_argument('--base', type=Path)
    parser.add_argument('--archive', type=Path, action='append', default=[])
    args = parser.parse_args()
    if args.action == 'archives':
        if len(args.archive) != 2:
            parser.error('archives requires the two actual runtime/sources archives')
        for path in args.archive:
            check_shipping_archive(path)
        return
    if args.root is None or args.snapshot is None:
        parser.error('snapshot/prepare require authoritative root and immutable snapshot')
    if args.action == 'snapshot':
        create_snapshot(args.root, args.snapshot)
    else:
        if args.run is None or args.base is None:
            parser.error('prepare requires --run and --base')
        require_native_receipt(args.run, args.base)
        prepare(args.root, args.snapshot, args.run, args.base)


if __name__ == '__main__':
    main()
