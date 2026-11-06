# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# The Rift Shard's, the Charged Rift Shard's and the Null Shard's item textures, drawn from code
# so no stand-in art of unknown source ships (#310). Run from the companion mod's root (MMR26.1.2/mod/)
# with Pillow installed:
#
#   python3 publish/make-shards.py
#
# It writes, all committed, 16 pixels wide, RGBA, in
# src/main/resources/assets/mechanicalmasteryreborn/textures/item/:
#   - rift_shard.png, the raw drop of an Assault's mobs: a chunk of the void's purple;
#   - charged_rift.png and charged_rift.png.mcmeta, that chunk once the Warding chain has charged
#     it: lit through in the Warding Essence's steel grey, lightning crackling down its length, in
#     the raw chunk's lilac and white, a crack of its own in each frame of a strip the .mcmeta
#     plays as Nullessence's is played, with no frame held;
#   - null_shard.png, the drop of an Assault taken on as a spell: the chunk gone to Nullessence's
#     near-black, keeping the raw chunk's purple rim so it still reads on a dark ground.
#
# One shape draws all three, so they read as the same drop in three states: a six-sided chunk
# broken off, leaning a little, as the polygon below. Its facets are bands by distance from its
# long axis, lit from the top left; each crack starts, jags and ends where a fixed seed sends it,
# so every run draws the same pixels. Nothing is read from another image: the palettes are the Warding Essence's and
# Nullessence's colours, copied in as numbers. The maintainer picked this shape from three
# candidates, and the crackling over sparks thrown clear of the chunk (#310).
import json
import random

from PIL import Image, ImageDraw

OUT = 'src/main/resources/assets/mechanicalmasteryreborn/textures/item/'
SIZE = 16
SEED = 310

# a palette: outline, the shaded facet, the lit facet, the highlight along the lit edge
RIFT = ((26, 8, 46), (74, 28, 128), (126, 60, 208), (196, 148, 255))  # the void's purple
WARD = ((40, 44, 52), (131, 140, 152), (169, 176, 185), (240, 242, 246))  # the Warding Essence's
NULL = ((20, 23, 16), (10, 12, 8), (32, 38, 26), (56, 64, 48))  # Nullessence's
FLASH = (255, 255, 255)  # the lightning at its brightest
GLOW = (196, 148, 255)  # the lightning between, the raw chunk's own light
FRAMES = 16  # the Charged Rift Shard's frames, each a crack of its own, lilac and white in turn
FRAMETIME = 2  # ticks a frame holds
CRACK_SIDE = -0.3  # how far toward the lit edge the crack may jag, against the half-width
CRACK_WANDER = 4  # how many rows from the head, and from the foot, a crack's ends may fall
# the facets, by distance across the chunk against its half-width: up to each limit, a band
BANDS = ((-0.5, 'bright'), (0.1, 'light'), (0.65, 'dark'), (9, 'deep'))

# the chunk: a polygon, as pixel corners on the 16x16, and its long axis, tip to tip
POLY = [(10, 1), (13, 5), (12, 11), (6, 14), (2, 12), (2, 7), (6, 2)]
AXIS = ((11, 1), (4, 14))


def across(p):
    # p's signed distance from the long axis, in pixels: negative toward the top left, where the
    # light is
    (ax, ay), (bx, by) = AXIS
    return ((by - ay) * (p[0] - ax) - (bx - ax) * (p[1] - ay)) / ((bx - ax) ** 2 + (by - ay) ** 2) ** 0.5


def chunk(pal):
    # the polygon filled in bands by distance from the axis against the chunk's half-width: the
    # highlight along the lit edge, the lit facet, the shaded facet, and the shade's far edge
    # darker still, under an outline where a neighbour lies outside
    mask = Image.new('L', (SIZE, SIZE), 0)
    ImageDraw.Draw(mask).polygon(POLY, fill=255)
    inside = lambda x, y: 0 <= x < SIZE and 0 <= y < SIZE and mask.getpixel((x, y)) > 0
    outline, dark, light, bright = pal
    shades = {'bright': bright, 'light': light, 'dark': dark,
              'deep': tuple((o + d) // 2 for o, d in zip(outline, dark))}
    cells = [(x, y) for y in range(SIZE) for x in range(SIZE) if inside(x, y)]
    half = max(abs(across(c)) for c in cells)
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    for x, y in cells:
        edge = not all(inside(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        t = across((x, y)) / half
        band = next(b for limit, b in BANDS if t < limit)
        img.putpixel((x, y), (outline if edge else shades[band]) + (255,))
    return img


def rift():
    return chunk(RIFT)


def crack(img, rng):
    # a jagged path down the chunk's inside, from a row near its head to one near its foot, both
    # the seed's pick as is where in its row it starts: one step down a row, with a step across
    # where the seed sends it, kept off the outline and off the lit edge's highlight, where a
    # white flash would vanish
    inside = lambda x, y: 0 <= x < SIZE and 0 <= y < SIZE and img.getpixel((x, y))[3] > 0
    half = max(abs(across((x, y))) for y in range(SIZE) for x in range(SIZE) if inside(x, y))
    body = lambda x, y: inside(x, y) and img.getpixel((x, y))[:3] != WARD[0] and across((x, y)) / half > CRACK_SIDE
    (_, hy), (_, fy) = AXIS
    top, bottom = rng.randint(hy + 1, hy + CRACK_WANDER), rng.randint(fy - CRACK_WANDER, fy - 1)
    x = rng.choice([x for x in range(SIZE) if body(x, top)])
    path = [(x, top)]
    for y in range(top + 1, bottom + 1):
        for dx in rng.sample((-1, 0, 1), 3):
            if body(x + dx, y) and (dx == 0 or body(x + dx, y - 1)):
                break
        if dx:
            path.append((x + dx, y - 1))
        x += dx
        path.append((x, y))
    return path


def charged():
    # the chunk lit through: the Warding palette, with a crack of its own in each frame's colour
    # down a strip of frames
    rng = random.Random(SEED)
    strip = Image.new('RGBA', (SIZE, SIZE * FRAMES), (0, 0, 0, 0))
    for i in range(FRAMES):
        frame = chunk(WARD)
        for x, y in crack(frame, rng):
            frame.putpixel((x, y), (GLOW, FLASH)[i % 2] + (255,))
        strip.paste(frame, (0, i * SIZE))
    return strip


def null():
    # the chunk gone dark, under the raw chunk's outline so it reads on a dark ground
    return chunk((RIFT[1],) + NULL[1:])


for name, draw in {'rift_shard': rift, 'charged_rift': charged, 'null_shard': null}.items():
    draw().save(f'{OUT}{name}.png')
with open(f'{OUT}charged_rift.png.mcmeta', 'w') as f:
    json.dump({'animation': {'frametime': FRAMETIME}}, f, indent=2)
    f.write('\n')
