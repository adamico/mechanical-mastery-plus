# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# The Vital Spark's art (#375), made from Malcolm Riley's unused-textures (NOTICE). Run from the companion
# mod's root (MMR26.1.2/mod/) with Pillow installed, given a checkout of
# https://github.com/malcolmriley/unused-textures:
#
#   python3 publish/make-vital-spark.py ~/minecraft_mods/unused-textures
#
# It writes, both committed, in src/main/resources/assets/mechanicalmasteryreborn/textures/item/:
#   - vital_spark.png, items/artifact_star.png with every pixel's hue turned to the Vital Port's green,
#     its saturation and brightness kept;
#   - vital_spark_spent.png, that star greyed: each pixel's luma times SPENT_DIM.
import colorsys
import sys

from PIL import Image

ITEM = 'src/main/resources/assets/mechanicalmasteryreborn/textures/item/'
VITAL = (101, 219, 101)  # the Vital Port's colour (NOTICE)
SPENT_DIM = 0.6  # how dark a spent spark's grey is, as a share of the new one's luma
HUE = colorsys.rgb_to_hsv(*(c / 255 for c in VITAL))[0]


def recoloured(img):
    out = img.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            _, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            px[x, y] = tuple(round(c * 255) for c in colorsys.hsv_to_rgb(HUE, s, v)) + (a,)
    return out


def spent(img):
    out = img.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            luma = round((0.3 * r + 0.59 * g + 0.11 * b) * SPENT_DIM)
            px[x, y] = (luma, luma, luma, a)
    return out


def main(textures):
    star = recoloured(Image.open(textures + '/items/artifact_star.png').convert('RGBA'))
    star.save(ITEM + 'vital_spark.png')
    spent(star).save(ITEM + 'vital_spark_spent.png')


if __name__ == '__main__':
    main(sys.argv[1])
