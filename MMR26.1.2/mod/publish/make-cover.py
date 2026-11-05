# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# Mechanical Mastery Reborn Core's cover and icon, in the -works suite's style, until an artist's art
# replaces them. Run from the companion mod's root (MMR26.1.2/mod/) with Pillow installed:
#
#   python3 publish/make-cover.py
#
# It writes, both committed:
#   - publish/mechanicalmasteryreborn-cover.png, 1280x640, the CurseForge project's cover;
#   - publish/mechanicalmasteryreborn-icon.png, 512x512, its icon, which must read at 64px.
#
# What it draws:
#   - a wall of the Recycler's casing, drive and vent textures, tiled nearest-neighbour as Beltworks'
#     tiles its belts, in a fixed pattern so every run draws the same image, dimmed so the title reads;
#   - the title band, dark, ruled top and bottom in the accent, holding the suite's title turned
#     around, as A Touch of Darkness's is: "MECHANICAL MASTERY" in Impact caps, in the accent's
#     orange, then the suite's white pill holding "core". The icon stacks them.
#
# Its art is the mod's own textures only, Malcolm Riley's Recycler textures (NOTICE), nothing of
# Mojang's. Its fonts are found on the system, with fallbacks; none is committed.
import os

from PIL import Image, ImageDraw, ImageFont

T = 'src/main/resources/assets/mechanicalmasteryreborn/textures/block/'
WALL = ['recycler_casing', 'recycler_drive', 'recycler_vent']
DIM = 0.45  # how far the wall is blended toward BG
BG = (14, 13, 16)
BAND = (8, 8, 11, 225)
ACCENT = (255, 136, 32)  # orange
PILL = (240, 240, 236)
PILL_TEXT = (24, 22, 30)
TITLE, WORD = 'MECHANICAL MASTERY', 'core'
IMPACT = ['/System/Library/Fonts/Supplemental/Impact.ttf', '/usr/share/fonts/TTF/impact.ttf',
          os.path.expanduser('~/.local/share/fonts/msfonts/impact.ttf'), '/usr/share/fonts/noto/NotoSans-Black.ttf']
BLACK = ['/System/Library/Fonts/Supplemental/Arial Black.ttf', '/usr/share/fonts/TTF/ariblk.ttf',
         os.path.expanduser('~/.local/share/fonts/msfonts/ariblk.ttf'), '/usr/share/fonts/noto/NotoSans-Black.ttf']


def wall(W, H, tile):
    # the wall centred on the image, each tile one of WALL by its row and column, so the drives and
    # vents fall in broken diagonals across the casing
    faces = [Image.open(f'{T}{n}.png').convert('RGBA').resize((tile, tile), Image.NEAREST) for n in WALL]
    img = Image.new('RGBA', (W, H), BG + (255,))
    x0, y0 = (W // 2) % tile - tile, (H // 2) % tile - tile
    for j, y in enumerate(range(y0, H, tile)):
        for i, x in enumerate(range(x0, W, tile)):
            img.alpha_composite(faces[(i + 2 * j) % len(faces)], (x, y))
    return Image.blend(img, Image.new('RGBA', (W, H), BG + (255,)), DIM)


def font(names, size):
    for n in names:
        if os.path.exists(n):
            return ImageFont.truetype(n, size)
    raise SystemExit('needs one of ' + ', '.join(names))


def make(W, H, out, title_size, tile, stacked=False):
    # tile is a wall tile's size, a whole multiple of the textures' 16 so their pixels stay square;
    # stacked breaks the title in two lines over the pill, for the square icon
    f1, f2 = font(IMPACT, title_size), font(BLACK, int(title_size * 0.62))
    lines = TITLE.split(' ') if stacked else [TITLE]
    d = ImageDraw.Draw(Image.new('RGB', (1, 1)))
    lb = [d.textbbox((0, 0), s, font=f1) for s in lines]
    b2 = d.textbbox((0, 0), WORD, font=f2)
    lh = max(b[3] - b[1] for b in lb)
    w2, h2 = b2[2] - b2[0], b2[3] - b2[1]
    pad, gap, sh = int(title_size * 0.16), int(title_size * 0.12), max(3, title_size // 20)
    bw, ph = w2 + 2 * pad, (h2 + 2 * int(title_size * 0.12) if stacked else lh)
    th = len(lines) * lh + (len(lines) - 1) * gap + (gap + ph if stacked else 0)
    cy, bh = H // 2, int(th + title_size * 0.7)
    top, bottom = cy - bh // 2, cy + bh // 2

    img = wall(W, H, tile)
    band = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(band).rectangle([0, top, W, bottom], fill=BAND)
    img = Image.alpha_composite(img, band)
    d = ImageDraw.Draw(img)
    lt = max(3, title_size // 25)
    d.rectangle([0, top, W, top + lt], fill=ACCENT)
    d.rectangle([0, bottom - lt, W, bottom], fill=ACCENT)

    y = cy - th // 2
    if stacked:
        places = [((W - (b[2] - b[0])) // 2, y + i * (lh + gap)) for i, b in enumerate(lb)]
        bx, bt = (W - bw) // 2, y + len(lines) * (lh + gap)
    else:
        w1 = lb[0][2] - lb[0][0]
        x = (W - (w1 + gap + bw)) // 2
        places = [(x, y)]
        bx, bt = x + w1 + gap, y
    for s, b, (x, ly) in zip(lines, lb, places):
        d.text((x + sh - b[0], ly + sh - b[1]), s, font=f1, fill=(0, 0, 0))
        d.text((x - b[0], ly - b[1]), s, font=f1, fill=ACCENT)
    d.rounded_rectangle([bx + sh, bt + sh, bx + bw + sh, bt + ph + sh], radius=pad, fill=(0, 0, 0))
    d.rounded_rectangle([bx, bt, bx + bw, bt + ph], radius=pad, fill=PILL)
    d.text((bx + pad - b2[0], bt + (ph - h2) // 2 - b2[1]), WORD, font=f2, fill=PILL_TEXT)
    img.convert('RGB').save(out)


make(1280, 640, 'publish/mechanicalmasteryreborn-cover.png', 104, 80)
make(512, 512, 'publish/mechanicalmasteryreborn-icon.png', 92, 64, stacked=True)
