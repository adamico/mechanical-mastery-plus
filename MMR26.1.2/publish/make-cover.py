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
# What it draws:
#   - the void: a near-black ground with faint specks, seeded, so every run draws the same image;
#   - above the title band, the four tier essences in a row, lowest first, scaled up nearest-neighbour,
#     each glowing in its own colour;
#   - the band, dark, ruled top and bottom in the accent, holding the suite's title turned around, as
#     A Touch of Darkness's is: "MECHANICAL MASTERY" in Chakra Petch caps, in the accent's
#     orange, then the suite's white pill holding "reborn". The icon stacks them;
#   - beneath the band, the four Transmuted Essences with Nullessence and Quintessence, the last
#     stand's two sides, paired in their middle.
#
# Its art is the companion mod's own textures only (mod/NOTICE), nothing of Mojang's. Its fonts are
# found on the system, with fallbacks; none is committed. Chakra Petch is a Google Font (SIL OFL),
# installed by hand, its Bold for the title and its Medium for the pill; without it the title falls
# back to Impact and the pill to Arial Black, which every run there draws the same, if not as designed.
import math
import os
import random

from PIL import Image, ImageDraw, ImageFont

T = 'mod/src/main/resources/assets/mechanicalmasteryreborn/textures/item/'
SEED = 26
BG = (6, 6, 10)
BAND = (4, 4, 7, 235)
ACCENT = (255, 136, 32)  # orange, a machine's warning paint, apart from every essence's colour
PILL = (240, 240, 236)
PILL_TEXT = (24, 22, 30)
# each essence glows in its texture's commonest colour, but Nullessence, near-black, glows white so it
# stands out of the void
GLOWS = {'nullessence': (255, 255, 255)}
# the four tiers, lowest first
TIERS = ['basic_mechanical_essence', 'regular_mechanical_essence', 'improved_mechanical_essence',
         'advanced_mechanical_essence']
# the Transmuted Essences, two each side of Nullessence and Quintessence; those two are animated, and
# their first frames are drawn
TRANSMUTED = ['galvanic_essence', 'warding_essence', 'vital_essence', 'aureate_essence']
LAST_STAND = ['nullessence', 'quintessence']
TITLE, WORD = 'MECHANICAL MASTERY', 'reborn'
FONTS = ['~/Library/Fonts/', '/Library/Fonts/', '~/.local/share/fonts/chakrapetch/', '~/.local/share/fonts/',
         '/usr/share/fonts/TTF/', '/usr/share/fonts/truetype/chakra-petch/']
TITLE_FONT = [d + 'ChakraPetch-Bold.ttf' for d in FONTS] + [
    '/System/Library/Fonts/Supplemental/Impact.ttf', '/usr/share/fonts/TTF/impact.ttf',
    '~/.local/share/fonts/msfonts/impact.ttf', '/usr/share/fonts/noto/NotoSans-Black.ttf']
WORD_FONT = [d + 'ChakraPetch-Medium.ttf' for d in FONTS] + [
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


def essence(img, name, cx, cy, px):
    g = glow(GLOWS.get(name) or colour(name), int(px * 1.5))
    img.alpha_composite(g, (cx - g.width // 2, cy - g.height // 2))
    s = texture(name).resize((px, px), Image.NEAREST)
    img.alpha_composite(s, (cx - px // 2, cy - px // 2))


def row(img, items, cy, px, gap):
    # items in a row centred on the width, px each with gap between them, or less where the width
    # holds no more
    gap = min(gap, (img.width - len(items) * px) // (len(items) + 1))
    x0 = (img.width - len(items) * px - (len(items) - 1) * gap) // 2 + px // 2
    for i, name in enumerate(items):
        essence(img, name, x0 + i * (px + gap), cy, px)


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


def make(out, W, H, title_size, px, under_px, stacked=False):
    # title_size is the title's largest size, drawn smaller where it would not fit; px and under_px are
    # the sizes of the essences above and beneath the band, whole multiples of the textures' 32 so
    # nearest-neighbour keeps their pixels square; stacked breaks the title in two lines over the
    # pill, for the square icon
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
    cy, bh = H // 2, int(th + title_size * 0.7)
    top, bottom = cy - bh // 2, cy + bh // 2

    img = void(W, H)
    row(img, TIERS, top // 2, px, px // 2)
    under = TRANSMUTED[:2] + LAST_STAND + TRANSMUTED[2:]
    row(img, under, (bottom + H) // 2, under_px, under_px * 2 // 3)

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


# each image: where it goes, its size, its title's largest size and its essences' sizes
COVER = ('publish/reborn-cover.png', 1280, 640, 96, 128, 96)
ICON = ('publish/reborn-icon.png', 512, 512, 92, 64, 64)
CARD = ('publish/reborn-card.png', 1920, 1080, 144, 192, 128)

make(*COVER)
make(*ICON, stacked=True)
make(*CARD)
