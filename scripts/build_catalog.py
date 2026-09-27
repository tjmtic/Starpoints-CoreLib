#!/usr/bin/env python3
"""Build Starpoints' bundled sky catalog from d3-celestial (BSD-3-Clause, Olaf Frohn).

    python3 scripts/build_catalog.py            # download the pinned sources, write catalog/
    python3 scripts/build_catalog.py --src DIR  # use already-downloaded files in DIR

Standard library only. The sources are pinned to one d3-celestial commit, so the same
command always writes the same bytes; catalog/SOURCES.txt records the commit and the
SHA-256 of every input and output.

Outputs, in catalog/:
  stars.8.spc               every star to magnitude 8 (41,411), in the SPC1 format read by
                            core-lib's readCatalog (Starpoints-CoreLib#9): 4 magic bytes "SPC1",
                            Int32 count, then per star Int32 id, Float32 RA, Float32 Dec,
                            Float32 magnitude, Float32 B-V, all little-endian; brightest first
  star-names.json           {"<hip>": "<proper name>"} for the stars in stars.8.spc
  constellations.json       [{"id", "name", "labelRa", "labelDec"}], the 88 IAU constellations;
                            Serpens is two parts (Caput, Cauda), so 89 entries, two with id Ser
  constellation-lines.json  {"<id>": [[[ra, dec], ...], ...]}: each constellation's stick figure
                            (88 ids; Serpens' two parts are one entry)
  ATTRIBUTION.md            the source, its license, and what was changed

Conventions: ids are Hipparcos numbers; RA and Dec are J2000 degrees, RA in [0, 360)
(d3-celestial stores RA in [-180, 180]); magnitudes are Hipparcos Hp as d3-celestial gives
them. Stars with no B-V in the source get 0.65, a neutral white, and are listed in
SOURCES.txt.
"""
import argparse
import hashlib
import json
import os
import struct
import sys
import urllib.request

COMMIT = '7e720a3de062059d4c5400a379146a601d9010e0'
BASE = f'https://raw.githubusercontent.com/ofrohn/d3-celestial/{COMMIT}/'
FILES = {
    'stars.8.json': 'data/stars.8.json',
    'starnames.json': 'data/starnames.json',
    'constellations.json': 'data/constellations.json',
    'constellations.lines.json': 'data/constellations.lines.json',
    'LICENSE': 'LICENSE',
}
MISSING_BV = 0.65
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'catalog')


def sha256(path):
    h = hashlib.sha256()
    with open(path, 'rb') as f:
        for chunk in iter(lambda: f.read(1 << 16), b''):
            h.update(chunk)
    return h.hexdigest()


def fetch(src_dir):
    os.makedirs(src_dir, exist_ok=True)
    for local, remote in FILES.items():
        path = os.path.join(src_dir, local)
        if not os.path.exists(path):
            print(f'downloading {remote}', file=sys.stderr)
            urllib.request.urlretrieve(BASE + remote, path)


def ra360(lon):
    ra = lon % 360.0
    return 0.0 if ra >= 360.0 else ra


def build(src_dir):
    load = lambda name: json.load(open(os.path.join(src_dir, name), encoding='utf-8'))
    stars = load('stars.8.json')['features']
    missing_bv = []
    records = []
    for f in stars:
        lon, dec = f['geometry']['coordinates']
        bv = f['properties'].get('bv')
        try:
            bv = float(bv)
        except (TypeError, ValueError):
            missing_bv.append(f['id'])
            bv = MISSING_BV
        records.append((int(f['id']), ra360(lon), float(dec), float(f['properties']['mag']), bv))
    records.sort(key=lambda r: (r[3], r[0]))  # brightest first; id breaks ties

    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, 'stars.8.spc'), 'wb') as out:
        out.write(b'SPC1')
        out.write(struct.pack('<i', len(records)))
        for rec in records:
            out.write(struct.pack('<iffff', *rec))

    in_catalog = {r[0] for r in records}
    names = {k: v['name'] for k, v in load('starnames.json').items() if v.get('name') and int(k) in in_catalog}
    names = dict(sorted(names.items(), key=lambda kv: int(kv[0])))
    write_json('star-names.json', names)

    cons = []
    for f in load('constellations.json')['features']:
        lon, lat = f['geometry']['coordinates']
        cons.append({'id': f['id'], 'name': f['properties']['name'], 'labelRa': round(ra360(lon), 4), 'labelDec': round(lat, 4)})
    write_json('constellations.json', sorted(cons, key=lambda c: c['id']))

    lines = {}
    for f in load('constellations.lines.json')['features']:
        # Serpens is two features with one id (Caput and Cauda): their figures are merged.
        lines.setdefault(f['id'], []).extend(
            [[round(ra360(lon), 4), round(lat, 4)] for lon, lat in poly] for poly in f['geometry']['coordinates'])
    write_json('constellation-lines.json', dict(sorted(lines.items())))

    with open(os.path.join(src_dir, 'LICENSE'), encoding='utf-8') as f:
        license_text = f.read().strip()
    with open(os.path.join(OUT, 'ATTRIBUTION.md'), 'w', encoding='utf-8') as f:
        f.write(ATTRIBUTION.format(commit=COMMIT, license=license_text, missing=len(missing_bv), bv=MISSING_BV))

    with open(os.path.join(OUT, 'SOURCES.txt'), 'w', encoding='utf-8') as f:
        f.write(f'd3-celestial commit {COMMIT}\n\ninputs (sha256):\n')
        for local in FILES:
            f.write(f'  {sha256(os.path.join(src_dir, local))}  {FILES[local]}\n')
        f.write('\noutputs (sha256):\n')
        for name in ('stars.8.spc', 'star-names.json', 'constellations.json', 'constellation-lines.json', 'ATTRIBUTION.md'):
            f.write(f'  {sha256(os.path.join(OUT, name))}  catalog/{name}\n')
        f.write(f'\nstars: {len(records)}; named: {len(names)}; constellations: {len(cons)}; '
                f'line polylines: {sum(len(v) for v in lines.values())}\n')
        f.write(f'stars with no B-V in the source (given {MISSING_BV}): {", ".join(str(i) for i in sorted(missing_bv))}\n')
    print(f'{len(records)} stars, {len(names)} names, {len(cons)} constellations -> {OUT}', file=sys.stderr)


def write_json(name, data):
    with open(os.path.join(OUT, name), 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, separators=(',', ':'))
        f.write('\n')


ATTRIBUTION = """# Catalog attribution

The files in this directory are derived from **d3-celestial** by Olaf Frohn
(https://github.com/ofrohn/d3-celestial), commit `{commit}`, used under the
BSD 3-Clause License reproduced below.

What was changed, by `scripts/build_catalog.py`:
- `stars.8.spc`: `data/stars.8.json` converted to the SPC1 binary format, right ascension moved
  from [-180, 180] to [0, 360), sorted brightest first. {missing} stars with no B-V in the source
  were given {bv}.
- `star-names.json`: the proper names from `data/starnames.json` for those stars.
- `constellations.json`: the English names and label positions from `data/constellations.json`.
- `constellation-lines.json`: `data/constellations.lines.json` with right ascension moved to [0, 360).

An app that ships these files must reproduce this notice and the license below in its
documentation or an in-app screen (clause 2).

## d3-celestial license

{license}
"""

if __name__ == '__main__':
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--src', help='directory with the downloaded d3-celestial files (default: download into build/d3-celestial)')
    args = ap.parse_args()
    src = args.src or os.path.join(ROOT, 'build', 'd3-celestial')
    if not args.src:
        fetch(src)
    build(src)
