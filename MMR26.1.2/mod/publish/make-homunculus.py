# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# The Homunculus's and its Homunculus Stand's art (#374), made from Malcolm Riley's unused-textures
# (NOTICE). Run from the companion mod's root (MMR26.1.2/mod/) with Pillow installed, given a checkout
# of https://github.com/malcolmriley/unused-textures:
#
#   uv run -q --no-project --with pillow python publish/make-homunculus.py ~/minecraft_mods/unused-textures
#
# It writes, all committed, in src/main/resources/assets/mechanicalmasteryreborn/textures/:
#   - entity/homunculus/homunculus.png, its 64x32 skin at the UVs HomunculusModel gives each box: the
#     clay of blocks/terracotta_tile_large's plain middle, its face drawn in darker clay;
#   - entity/homunculus/homunculus_statue.png, the same greyed, as it stands while it sleeps;
#   - block/homunculus_stand_clay.png, the stand's lower half: blocks/terracotta_tile_large, copied
#     unmodified;
#   - block/homunculus_stand_rod.png and homunculus_stand_rod_end.png, its upper half's rod:
#     blocks/copper_coil_side and copper_coil_end, copied unmodified;
#
# and in models/block/ the stand's two halves, homunculus_stand_lower.json (a clay block) and
# homunculus_stand_upper.json (a copper rod, capped, inside HomunculusStandBlock's rod shape).
import json
import math
import shutil
import sys

from PIL import Image

ASSETS = 'src/main/resources/assets/mechanicalmasteryreborn/'
ENTITY = ASSETS + 'textures/entity/homunculus/'
BLOCK = ASSETS + 'textures/block/'
MODELS = ASSETS + 'models/block/'
TEXTURE = 'mechanicalmasteryreborn:block/'
CLAY = (2, 2, 14, 14)  # terracotta_tile_large's plain middle, inside its border

# (u, v, w, h, d, darkness), as the model's texOffs and box sizes; darkness shades the clay.
BOXES = [
    (0, 0, 7, 9, 7, 1.0),       # head
    (28, 0, 4, 4, 2.5, 1.0),    # body
    (42, 0, 2, 3, 2, 0.92),     # nose
    (50, 0, 1, 3, 2, 0.95),     # ears
    (56, 0, 1, 3, 2, 0.95),
    (42, 7, 2, 1, 1.5, 0.85),   # neck
    (0, 16, 1, 5.5, 1, 0.97),   # arms
    (5, 16, 1, 5.5, 1, 0.97),
    (10, 16, 1.5, 3, 1.5, 0.88),  # legs
    (17, 16, 1.5, 3, 1.5, 0.88),
    (24, 16, 1.5, 1, 2.5, 0.85),  # feet
    (33, 16, 1.5, 1, 2.5, 0.85),
]
EYE, MOUTH, LINE = 0.35, 0.55, 0.8  # the face's shades of the clay


def scaled(pixel, k):
    return tuple(max(0, min(255, round(c * k))) for c in pixel[:3]) + (255,)


def skin(tile):
    clay = tile.crop(CLAY)
    w, h = clay.size
    img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    px = img.load()

    def at(x, y, k=1.0):
        return scaled(clay.getpixel((x % w, y % h)), k)

    for u, v, bw, bh, d, k in BOXES:
        for y in range(v, math.ceil(v + d + bh)):
            for x in range(u, math.ceil(u + 2 * (d + bw))):
                if y < v + d and not (u + d <= x + 0.5 < u + d + 2 * bw):
                    continue
                px[x, y] = at(x, y, k)
    # The face, on the head's front: columns 7..13, rows 7 (crown) to 15 (chin).
    fx, fy = 7, 7
    for col in (1, 5):
        px[fx + col, fy + 3] = at(fx + col, fy + 3, EYE)
    for col in range(1, 6):
        px[fx + col, fy + 1] = at(fx + col, fy + 1, LINE)
    for col in (0, 6):
        px[fx + col, fy + 2] = at(fx + col, fy + 2, LINE)
    for col in range(2, 5):
        px[fx + col, fy + 8] = at(fx + col, fy + 8, MOUTH)
    px[fx + 1, fy + 7] = at(fx + 1, fy + 7, LINE)
    px[fx + 5, fy + 7] = at(fx + 5, fy + 7, LINE)
    return img


def statue(img):
    grey = img.copy()
    px = grey.load()
    for y in range(grey.height):
        for x in range(grey.width):
            r, g, b, a = px[x, y]
            if a:
                l = int(0.3 * r + 0.59 * g + 0.11 * b)
                l = int(l * 0.75 + 40)
                px[x, y] = (l, l, l, a)
    return grey


def box(start, end, sides, top, bottom=None, cull_bottom=False):
    """A box from start to end: its four sides from sides, its top and bottom faces as given."""
    x0, y0, z0 = start
    x1, y1, z1 = end
    side_uv = {'north': [16 - x1, 16 - y1, 16 - x0, 16 - y0], 'south': [x0, 16 - y1, x1, 16 - y0],
               'east': [16 - z1, 16 - y1, 16 - z0, 16 - y0], 'west': [z0, 16 - y1, z1, 16 - y0]}
    faces = {side: {'texture': sides, 'uv': uv} for side, uv in side_uv.items()}
    faces['up'] = {'texture': top, 'uv': [x0, z0, x1, z1]}
    faces['down'] = {'texture': bottom or top, 'uv': [x0, z0, x1, z1]}
    if cull_bottom:
        faces['down']['cullface'] = 'down'
    return {'from': list(start), 'to': list(end), 'faces': faces}


def upper():
    return {'parent': 'minecraft:block/block',
            'textures': {'particle': TEXTURE + 'homunculus_stand_rod', 'rod': TEXTURE + 'homunculus_stand_rod',
                         'end': TEXTURE + 'homunculus_stand_rod_end'},
            'elements': [box((7, 0, 7), (9, 12, 9), '#rod', '#end', cull_bottom=True),
                         box((6, 12, 6), (10, 16, 10), '#rod', '#end')]}


def lower():
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': TEXTURE + 'homunculus_stand_clay'}}


def write_json(value, path):
    with open(path, 'w') as out:
        json.dump(value, out, indent=2)
        out.write('\n')


def main(textures):
    blocks = textures + '/blocks/'
    tile = Image.open(blocks + 'terracotta_tile_large.png').convert('RGBA')
    img = skin(tile)
    img.save(ENTITY + 'homunculus.png')
    statue(img).save(ENTITY + 'homunculus_statue.png')
    shutil.copyfile(blocks + 'terracotta_tile_large.png', BLOCK + 'homunculus_stand_clay.png')
    shutil.copyfile(blocks + 'copper_coil_side.png', BLOCK + 'homunculus_stand_rod.png')
    shutil.copyfile(blocks + 'copper_coil_end.png', BLOCK + 'homunculus_stand_rod_end.png')
    write_json(lower(), MODELS + 'homunculus_stand_lower.json')
    write_json(upper(), MODELS + 'homunculus_stand_upper.json')


if __name__ == '__main__':
    main(sys.argv[1])
