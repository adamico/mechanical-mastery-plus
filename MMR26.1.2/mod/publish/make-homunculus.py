# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT
"""Paints the Homunculus's 64x32 texture and its grey statue copy, at the UVs HomunculusModel gives each box.

uv run -q --no-project --with pillow python publish/make-homunculus.py src/main/resources/assets/mechanicalmasteryreborn/textures/entity/homunculus
"""
import math, random, sys
from PIL import Image

SKIN = (196, 160, 140); DARK = (160, 124, 108); NOSE = (184, 136, 122)
EYE = (40, 26, 24); MOUTH = (110, 64, 60); WRINKLE = (172, 136, 118); DOME = (206, 172, 152)

# (u, v, w, h, d, colour), as the model's texOffs and box sizes.
BOXES = [
    (0, 0, 7, 9, 7, SKIN),      # head
    (28, 0, 4, 4, 2.5, SKIN),   # body
    (42, 0, 2, 3, 2, NOSE),     # nose
    (50, 0, 1, 3, 2, SKIN),     # ears
    (56, 0, 1, 3, 2, SKIN),
    (42, 7, 2, 1, 1.5, DARK),   # neck
    (0, 16, 1, 5.5, 1, SKIN),   # arms
    (5, 16, 1, 5.5, 1, SKIN),
    (10, 16, 1.5, 3, 1.5, DARK),  # legs
    (17, 16, 1.5, 3, 1.5, DARK),
    (24, 16, 1.5, 1, 2.5, DARK),  # feet
    (33, 16, 1.5, 1, 2.5, DARK),
]

def shade(c, rng):
    k = rng.uniform(-6, 6)
    return tuple(max(0, min(255, int(x + k))) for x in c) + (255,)

def paint():
    rng = random.Random(367)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    px = img.load()
    for u, v, w, h, d, c in BOXES:
        for y in range(v, math.ceil(v + d + h)):
            for x in range(u, math.ceil(u + 2 * (d + w))):
                top = y < v + d
                if top and not (u + d <= x + 0.5 < u + d + 2 * w):
                    continue
                px[x, y] = shade(c, rng)
    # The head's top, a little lighter: the dome of the skull.
    for y in range(0, 7):
        for x in range(7, 14):
            px[x, y] = shade(DOME, rng)
    # The face, on the head's front: columns 7..13, rows 7 (crown) to 15 (chin).
    fx, fy = 7, 7
    for col in (1, 5):
        px[fx + col, fy + 3] = EYE + (255,)
    for col in range(1, 6):
        px[fx + col, fy + 1] = shade(WRINKLE, rng)
    for col in (0, 6):
        px[fx + col, fy + 2] = shade(WRINKLE, rng)
    for col in range(2, 5):
        px[fx + col, fy + 8] = MOUTH + (255,)
    px[fx + 1, fy + 7] = shade(WRINKLE, rng)
    px[fx + 5, fy + 7] = shade(WRINKLE, rng)
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

out = sys.argv[1]
img = paint()
img.save(f"{out}/homunculus.png")
statue(img).save(f"{out}/homunculus_statue.png")
