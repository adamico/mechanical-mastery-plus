# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# Mechanical Mastery Reborn's cover, icon and video card, in the -works suite's style, until an
# artist's logo replaces them. Run from the pack's root (MMR26.1.2/) with Pillow installed:
#
#   python3 publish/make-cover.py
#
# It writes, all committed:
#   - publish/reborn-cover.png, 1280x640, the CurseForge project's cover;
#   - publish/reborn-icon.png, 512x512, its icon, which must read at 64px;
#   - publish/reborn-card.png, 1920x1080, the video's title and end card.
#
# What it draws, after the very first Mechanical Mastery's logo, its island on an orange sky:
#   - the void: a near-black ground with faint specks, seeded, so every run draws the same image, and
#     an orange glow behind the island, in the accent;
#   - the team's starting island in the middle, drawn from its template (ISLAND) block by block, a
#     quarter turned so its tree stands at the back, each block a cube in a flat colour of its own,
#     its top lighter than its sides; a Port takes its essence's colour;
#   - on the cover and the card, the essences by kind, each scaled up nearest-neighbour and glowing in
#     its colour: left of the island the four tiers in a diamond, lowest at the top; right of it the
#     four Transmuted Essences in a diamond; and Nullessence and Quintessence, the last stand's two
#     sides, larger, overlapping each other over the tree's leaves; the icon, too small for them, keeps
#     the island alone;
#   - the band across the island's earth, as the old logo's letters crossed it, dark but letting the
#     island show through, ruled top and bottom in the accent, holding the suite's title turned
#     around, as A Touch of Darkness's is: "MECHANICAL MASTERY" in Barlow Condensed caps, in the
#     accent's orange, then the suite's white pill holding "reborn". The icon stacks them.
#
# Its art is the companion mod's own textures and flat colours only (mod/NOTICE), nothing of
# Mojang's: the island's vanilla blocks are colours, not textures. Its fonts are
# found on the system, with fallbacks; none is committed. Barlow Condensed and Barlow are Google Fonts
# (SIL OFL), installed by hand, the first's Black for the title, as heavy as it comes so it stands
# out, and the second's Bold for the pill; without them the title falls back to Impact and the pill to
# Arial Black, which every run there draws the same, if not as designed.
import math
import os
import random
import re

from PIL import Image, ImageDraw, ImageFont

T = 'mod/src/main/resources/assets/mechanicalmasteryreborn/textures/item/'
SEED = 26
BG = (6, 6, 10)
BAND = (4, 4, 7, 170)  # dark enough for the title, light enough for the island beneath
ACCENT = (255, 136, 32)  # orange, a machine's warning paint, apart from every essence's colour
PILL = (240, 240, 236)
PILL_TEXT = (24, 22, 30)
# each essence glows in its texture's commonest colour, but Nullessence, near-black, glows white so it
# stands out of the void
GLOWS = {'nullessence': (255, 255, 255)}
# the four tiers, lowest first
TIERS = ['basic_mechanical_essence', 'regular_mechanical_essence', 'improved_mechanical_essence',
         'advanced_mechanical_essence']
# the Transmuted Essences, and Nullessence and Quintessence, the last stand's two sides; those two are
# animated, and their first frames are drawn
TRANSMUTED = ['galvanic_essence', 'warding_essence', 'vital_essence', 'aureate_essence']
LAST_STAND = ['nullessence', 'quintessence']
# where the tier essences' diamond and the Transmuted Essences' stand, as fractions of the width and
# height, and how far a diamond's points are from its middle, in essences
LEFT, RIGHT, SPREAD = (0.14, 0.34), (0.86, 0.34), 1.0
# where Nullessence and Quintessence stand, over the tree's leaves, how much larger than the others they
# are drawn, and how far each stands off that middle, in their own size
PAIR, PAIR_SCALE, OVERLAP = (0.5, 0.22), 3, 0.18
ISLAND = 'config/skyblockbuilder/templates/islands/reborn.snbt'
# each of the island's blocks as the (top, side) colour it is drawn in; a Port's is its essence's
BLOCKS = {'stone': ((128, 128, 132),) * 2, 'dirt': ((134, 96, 66),) * 2,
          'grass_block': ((106, 170, 64), (134, 96, 66)), 'water': ((60, 100, 210),) * 2,
          'lava': ((240, 120, 30),) * 2, 'oak_log': ((176, 140, 88), (104, 82, 52)),
          'oak_leaves': ((58, 120, 40),) * 2, 'cod': ((64, 64, 72),) * 2, 'lab': ((92, 88, 104),) * 2}
PORTS = ['galvanic', 'warding', 'vital', 'aureate']
LIGHT = (0.8, 0.62)  # the island's left and right faces against its top
VIEW = (0.9, 0.3)  # how wide a block's diagonal is drawn and how far its top tilts toward the camera
SKY = 200  # the orange glow's strength at its middle
TITLE, WORD = 'MECHANICAL MASTERY', 'reborn'
FONTS = ['~/Library/Fonts/', '/Library/Fonts/', '~/.local/share/fonts/barlow/', '~/.local/share/fonts/',
         '/usr/share/fonts/TTF/', '/usr/share/fonts/truetype/barlow/']
TITLE_FONT = [d + 'BarlowCondensed-Black.ttf' for d in FONTS] + [
    '/System/Library/Fonts/Supplemental/Impact.ttf', '/usr/share/fonts/TTF/impact.ttf',
    '~/.local/share/fonts/msfonts/impact.ttf', '/usr/share/fonts/noto/NotoSans-Black.ttf']
WORD_FONT = [d + 'Barlow-Bold.ttf' for d in FONTS] + [
    '/System/Library/Fonts/Supplemental/Arial Black.ttf', '/usr/share/fonts/TTF/ariblk.ttf',
    '~/.local/share/fonts/msfonts/ariblk.ttf', '/usr/share/fonts/noto/NotoSans-Black.ttf']
WORD_SCALE = 0.7  # the pill's word against the title
FIT = 0.92  # the most of the width the title takes; a wider font is drawn smaller


def texture(name):
    im = Image.open(f'{T}{name}.png').convert('RGBA')
    return im.crop((0, 0, im.width, im.width))


def colour(name):
    # the texture's commonest opaque colour
    return max((n, p) for n, p in texture(name).getcolors(1 << 16) if p[3])[1][:3]


def glow(rgb, r, peak=140):
    # a pool of light falling off to nothing in the void
    a = [int(peak * (1 - d) ** 2) if d < 1 else 0
         for y in range(2 * r) for x in range(2 * r) for d in [math.hypot(x - r + 0.5, y - r + 0.5) / r]]
    im = Image.new('RGBA', (2 * r, 2 * r), rgb + (0,))
    mask = Image.new('L', im.size)
    mask.putdata(a)
    im.putalpha(mask)
    return im


def essences(img, placed, px):
    # each (name, x, y) essence px wide, every glow laid first so none washes over a neighbour
    for name, x, y in placed:
        g = glow(GLOWS.get(name) or colour(name), int(px * 1.5))
        img.alpha_composite(g, (x - g.width // 2, y - g.height // 2))
    for name, x, y in placed:
        img.alpha_composite(texture(name).resize((px, px), Image.NEAREST), (x - px // 2, y - px // 2))


def diamond(img, names, at, px):
    # four essences at a diamond's top, left, right and bottom points, about at, a fraction of the image
    cx, cy, r = int(at[0] * img.width), int(at[1] * img.height), int(SPREAD * px)
    return [(n, cx + dx * r, cy + dy * r) for n, (dx, dy) in zip(names, [(0, -1), (-1, 0), (1, 0), (0, 1)])]


def blocks():
    # the island's blocks as (x, y, z) and their colours, a quarter turned so the tree stands at the back
    src = open(ISLAND).read()
    palette = re.findall(r'"([^"]+)"', src[src.index('palette'):])
    colours = dict(BLOCKS, **{p + '_port': (colour(p + '_essence'),) * 2 for p in PORTS})
    out = []
    for x, y, z, state in re.findall(r'pos: \[(\d+), (\d+), (\d+)\], state: (\d+|"[^"]+")', src):
        state = palette[int(state)] if state.isdigit() else state.strip('"')
        name = state.split('{')[0].split(':')[1]
        if name not in colours:
            raise SystemExit(f'no colour for {state} in {ISLAND}')
        out.append(((8 - int(z), int(y), int(x)), colours[name]))
    return out


def island(u):
    # the island, u pixels to a block: each block's top and two front faces, back to front, a face
    # another block covers left out
    bs = blocks()
    solid = {b for b, _ in bs}
    wide, tilt = VIEW
    p = lambda x, y, z: ((x - z) * wide * u, ((x + z) * tilt - y) * u)
    corners = [p(x + i, y + j, z + k) for (x, y, z), _ in bs for i in (0, 1) for j in (0, 1) for k in (0, 1)]
    ox, oy = -min(c[0] for c in corners), -min(c[1] for c in corners)
    im = Image.new('RGBA', (int(max(c[0] for c in corners) + ox) + 2, int(max(c[1] for c in corners) + oy) + 2))
    d = ImageDraw.Draw(im)
    q = lambda x, y, z: (p(x, y, z)[0] + ox, p(x, y, z)[1] + oy)
    shade = lambda c, f: tuple(int(v * f) for v in c)
    for (x, y, z), (top, side) in sorted(bs, key=lambda b: (b[0][0] + b[0][2], b[0][1])):
        if (x, y + 1, z) not in solid:
            d.polygon([q(x, y + 1, z), q(x + 1, y + 1, z), q(x + 1, y + 1, z + 1), q(x, y + 1, z + 1)], fill=top)
        if (x + 1, y, z) not in solid:
            d.polygon([q(x + 1, y + 1, z), q(x + 1, y + 1, z + 1), q(x + 1, y, z + 1), q(x + 1, y, z)],
                      fill=shade(side, LIGHT[1]))
        if (x, y, z + 1) not in solid:
            d.polygon([q(x, y + 1, z + 1), q(x + 1, y + 1, z + 1), q(x + 1, y, z + 1), q(x, y, z + 1)],
                      fill=shade(side, LIGHT[0]))
    return im


def sky(W, H, cx, cy):
    # the orange glow behind the island, falling off into the void
    r = max(W, H) * 0.55
    a = Image.new('L', (W, H))
    a.putdata([int(SKY * max(0, 1 - math.hypot(x - cx, (y - cy) * 1.1) / r) ** 1.5)
               for y in range(H) for x in range(W)])
    im = Image.new('RGBA', (W, H), ACCENT + (0,))
    im.putalpha(a)
    return im


def void(W, H):
    rng = random.Random(SEED)
    img = Image.new('RGBA', (W, H), BG + (255,))
    d = ImageDraw.Draw(img)
    for _ in range(W * H // 700):
        v = rng.randrange(14, 34)
        d.point((rng.randrange(W), rng.randrange(H)), fill=(v, v, v + 6))
    return img


def font(names, size):
    for n in map(os.path.expanduser, names):
        if os.path.exists(n):
            return ImageFont.truetype(n, size)
    raise SystemExit('needs one of ' + ', '.join(names))


def make(out, W, H, title_size, band_y, island_h, island_y, px=None, stacked=False):
    # title_size is the title's size, drawn smaller where it would not fit, as with a fallback font;
    # band_y is where the band's middle is, island_h how much of the height the island takes and
    # island_y where its top is, each a fraction of the height; px is the essences' size, a whole
    # multiple of the textures' 32 so nearest-neighbour keeps their pixels square, or None to leave
    # them out; stacked breaks the title in two lines over the pill, for the square icon
    lines = TITLE.split(' ') if stacked else [TITLE]
    d = ImageDraw.Draw(Image.new('RGB', (1, 1)))
    while True:
        f1, f2 = font(TITLE_FONT, title_size), font(WORD_FONT, int(title_size * WORD_SCALE))
        lb = [d.textbbox((0, 0), s, font=f1) for s in lines]
        b2 = d.textbbox((0, 0), WORD, font=f2)
        lh = max(b[3] - b[1] for b in lb)
        w2, h2 = b2[2] - b2[0], b2[3] - b2[1]
        pad, gap, sh = int(title_size * 0.16), int(title_size * 0.12), max(3, title_size // 20)
        bw, ph = w2 + 2 * pad, (h2 + 2 * int(title_size * 0.12) if stacked else max(lh, h2 + 2 * pad))
        widths = [b[2] - b[0] for b in lb]
        if (max(widths + [bw]) if stacked else widths[0] + gap + bw) <= FIT * W:
            break
        title_size -= 2
    th = len(lines) * lh + (len(lines) - 1) * gap + gap + ph if stacked else ph
    cy, bh = int(band_y * H), int(th + title_size * 0.7)
    top, bottom = cy - bh // 2, cy + bh // 2

    img = void(W, H)
    u = 1
    while island(u + 1).height <= island_h * H:
        u += 1
    land = island(u)
    at = ((W - land.width) // 2, int(island_y * H))
    img.alpha_composite(sky(W, H, W // 2, at[1] + land.height * 0.4))
    img.alpha_composite(land, at)
    if px:
        essences(img, diamond(img, TIERS, LEFT, px), px)
        essences(img, diamond(img, TRANSMUTED, RIGHT, px), px)
        big = PAIR_SCALE * px
        mx, my, o = int(PAIR[0] * W), int(PAIR[1] * H), int(OVERLAP * big)
        essences(img, [(LAST_STAND[0], mx - o, my - o), (LAST_STAND[1], mx + o, my + o)], big)

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
        w1 = widths[0]
        x = (W - (w1 + gap + bw)) // 2
        places = [(x, y + (ph - lh) // 2)]
        bx, bt = x + w1 + gap, y
    for s, b, (x, ly) in zip(lines, lb, places):
        d.text((x + sh - b[0], ly + sh - b[1]), s, font=f1, fill=(0, 0, 0))
        d.text((x - b[0], ly - b[1]), s, font=f1, fill=ACCENT)
    d.rounded_rectangle([bx + sh, bt + sh, bx + bw + sh, bt + ph + sh], radius=pad, fill=(0, 0, 0))
    d.rounded_rectangle([bx, bt, bx + bw, bt + ph], radius=pad, fill=PILL)
    d.text((bx + pad - b2[0], bt + (ph - h2) // 2 - b2[1]), WORD, font=f2, fill=PILL_TEXT)
    img.convert('RGB').save(out)


# each image: where it goes, its size, its title's size, where its band is, the island's height and
# top, and its essences' size
COVER = ('publish/reborn-cover.png', 1280, 640, 108, 0.76, 0.92, 0.04, 64)
ICON = ('publish/reborn-icon.png', 512, 512, 100, 0.74, 0.62, 0.02)
CARD = ('publish/reborn-card.png', 1920, 1080, 162, 0.77, 0.9, 0.04, 96)

make(*COVER)
make(*ICON, stacked=True)
make(*CARD)
