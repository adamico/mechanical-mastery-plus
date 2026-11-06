# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# The Rift Shard's, the Charged Rift Shard's and the Null Shard's item textures, drawn from code
# so no stand-in art of unknown source ships (#310). Run from the companion mod's root
# (MMR26.1.2/mod/) with Pillow installed:
#
#   python3 publish/make-shards.py
#
# It writes, all committed, 16 pixels wide, RGBA, in
# src/main/resources/assets/mechanicalmasteryreborn/textures/item/:
#   - rift_shard.png, the raw drop of an Assault's mobs: a chunk of the void's purple;
#   - charged_rift.png and charged_rift.png.mcmeta, that chunk once the Warding chain has charged
#     it: lit through in the Warding Essence's steel grey, white lightning crackling down its
#     middle and out past its ends, a bolt of its own in the frames that carry one, fading over
#     the frame after, the chunk alone between, on a strip of frames the .mcmeta plays through
#     at an even pace, none held;
#   - null_shard.png, the drop of an Assault taken on as a spell: the chunk gone to Nullessence's
#     near-black, keeping the raw chunk's purple rim so it still reads on a dark ground.
#
# One shape draws all three, so they read as the same drop in three states: a six-sided chunk
# broken off, leaning a little, as the polygon below. Its facets are bands by distance from its
# long axis, lit from the top left; each bolt starts, jags and ends where a fixed seed sends it,
# so every run draws the same pixels. Nothing is read from another image: the palettes are the
# Warding Essence's and Nullessence's colours, copied in as numbers. The maintainer picked this
# shape from three candidates, and the bolts over sparks thrown clear of the chunk (#310).
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
BOLT = (255, 255, 255)  # the lightning, white
FRAMES = 16  # the Charged Rift Shard's frames, two ticks each, none held
BOLT_CHANCE = 0.6  # the odds a frame carries a bolt; a bolt's fade takes the frame after it
FADE = 0.45  # how much of a bolt the frame after it keeps, as it fades
FORK_CHANCE = 0.5  # the odds a bolt forks
FORK_LENGTH = 3  # a fork's length, in pixels
FRAMETIME = 2  # ticks a frame holds
BOLT_WANDER = 5  # how many rows at the top, and at the bottom, a bolt's ends may fall in
BOLT_JAG = 2  # the most pixels a bolt steps across in one row
BOLT_LEAN = (-3, 8)  # how many columns right of its head a bolt's foot falls, least to most
BOLT_OFFSET = 1  # how far from the chunk's middle, either way, a bolt's line crosses it
BOLT_SPREAD = 1.5  # how far a bolt may stray from its own line, in pixels
# the facets, by distance across the chunk against its half-width: up to each limit, a band
BANDS = ((-0.5, 'bright'), (0.1, 'light'), (0.65, 'dark'), (9, 'deep'))

# the chunk: a polygon, as pixel corners on the 16x16, and its long axis, tip to tip
POLY = [(10, 1), (13, 5), (12, 11), (6, 14), (2, 12), (2, 7), (6, 2)]
AXIS = ((11, 1), (4, 14))


def across(p):
    # p's signed distance from the long axis, in pixels: negative toward the top left, where the
    # light is
    (ax, ay), (bx, by) = AXIS
    length = ((bx - ax) ** 2 + (by - ay) ** 2) ** 0.5
    return ((by - ay) * (p[0] - ax) - (bx - ax) * (p[1] - ay)) / length


def half_width():
    mask = Image.new('L', (SIZE, SIZE), 0)
    ImageDraw.Draw(mask).polygon(POLY, fill=255)
    return max(abs(across((x, y)))
               for y in range(SIZE) for x in range(SIZE) if mask.getpixel((x, y)) > 0)


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
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    for x, y in [(x, y) for y in range(SIZE) for x in range(SIZE) if inside(x, y)]:
        edge = not all(inside(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        t = across((x, y)) / HALF
        band = next(b for limit, b in BANDS if t < limit)
        img.putpixel((x, y), (outline if edge else shades[band]) + (255,))
    return img


HALF = half_width()  # the chunk's half-width, the furthest its pixels lie from the axis


def rift():
    return chunk(RIFT)


def bolt(rng):
    # a jagged path down the canvas, longer than the chunk, from the seed's pick of a row near the
    # top to its pick of a row near the bottom, so no two bolts are of a length, on a line of its
    # own through the chunk's middle, leaning by the seed's pick, mostly from the top left to the
    # bottom right; it walks one row at a time, up to BOLT_JAG pixels across where the seed sends
    # it, kept close to its line; now and then a short fork off it
    top, bottom = rng.randint(0, BOLT_WANDER - 1), rng.randint(SIZE - BOLT_WANDER, SIZE - 1)
    (ax, ay), (bx, by) = AXIS
    mx, my = (ax + bx) / 2 + rng.randint(-BOLT_OFFSET, BOLT_OFFSET), (ay + by) / 2
    lean = rng.randint(*BOLT_LEAN)
    x0 = min(SIZE - 1, max(0, round(mx - lean * (my - top) / (bottom - top))))
    x1 = min(SIZE - 1, max(0, round(mx + lean * (bottom - my) / (bottom - top))))
    line = lambda y: x0 + (x1 - x0) * (y - top) / (bottom - top)
    near = lambda x, y: 0 <= x < SIZE and abs(x - line(y)) <= BOLT_SPREAD
    steps = list(range(-BOLT_JAG, BOLT_JAG + 1))
    x, path = x0, [(x0, top)]
    for y in range(top + 1, bottom + 1):
        rng.shuffle(steps)
        dx = next((dx for dx in steps if near(x + dx, y)), None)
        if dx is None:
            dx = max(-BOLT_JAG, min(BOLT_JAG, round(line(y)) - x))
        path += [(x + d, y - 1) for d in range(min(dx, 0), max(dx, 0) + 1) if d]
        x += dx
        path.append((x, y))
    if rng.random() < FORK_CHANCE:
        fx, fy = rng.choice(path[1:-1])
        d = rng.choice((-1, 1))
        path += [(fx + d * i, fy + i) for i in range(1, FORK_LENGTH + 1)
                 if 0 <= fx + d * i < SIZE and fy + i < SIZE]
    return path


def faded(frame, path):
    # frame with the bolt along path at FADE of its strength: blended into the chunk where it
    # crosses it, and the bolt's colour part-clear beside it
    fade = frame.copy()
    for x, y in path:
        r, g, b, a = fade.getpixel((x, y))
        if a:
            blend = tuple(round(c * (1 - FADE) + w * FADE) for c, w in zip((r, g, b), BOLT))
            fade.putpixel((x, y), blend + (255,))
        else:
            fade.putpixel((x, y), BOLT + (round(255 * FADE),))
    return fade


def charged():
    # the chunk lit through: the Warding palette, and over the frames the seed picks for one, all
    # but the last, a white bolt of its own, which fades over the frame after rather than
    # vanishing; down a strip of frames
    rng = random.Random(SEED)
    strip = Image.new('RGBA', (SIZE, SIZE * FRAMES), (0, 0, 0, 0))
    fade = None
    for i in range(FRAMES):
        frame = fade or chunk(WARD)
        fade = None
        if i < FRAMES - 1 and rng.random() < BOLT_CHANCE:
            path = bolt(rng)
            fade = faded(frame, path)
            for x, y in path:
                frame.putpixel((x, y), BOLT + (255,))
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
