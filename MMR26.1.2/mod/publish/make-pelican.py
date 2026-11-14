# SPDX-FileCopyrightText: 2026 5thlayer
# SPDX-License-Identifier: MIT

# The Pelican's block textures and models (#371), the textures made from Malcolm Riley's
# unused-textures (NOTICE). Run from the companion mod's root (MMR26.1.2/mod/) with Pillow
# installed, given a checkout of https://github.com/malcolmriley/unused-textures:
#
#   python3 publish/make-pelican.py ~/minecraft_mods/unused-textures
#
# It writes, all committed, in src/main/resources/assets/mechanicalmasteryreborn/textures/block/:
#   - pelican_bronze.png, its base: blocks/block_metal_black_bronze_block_1, copied unmodified;
#   - pelican_glass.png, its belly, neck and arms: blocks/glass_crystal_0, its clear pixels filled
#     with a faint pale glass so the vessel reads as glass, not as a wire frame;
#   - pelican_contents.png, what its belly holds while it eats: the first frame of
#     fluids/fluid_molten recoloured to the Vital Port's colour, as the mint's molten metals are,
#     then dimmed;
#   - pelican_contents_lit.png and its .mcmeta, what its belly holds while it digests: the whole
#     fluid_molten strip recoloured the same way at full brightness, its .mcmeta copied unmodified;
#   - pelican_drip.png and its .mcmeta, what runs down its arms back into the belly while it
#     digests: the drops of items/alchemy_pot_contents_dripping, in the Vital Port's colour,
#     falling a pixel a frame and wrapping round.
#
# and in models/block/, from one description of the vessel so the two never drift apart:
#   - pelican.json, the Pelican while it eats: a bronze base, a glass belly with a shoulder, a neck
#     rising from it into a bar, and an arm down each end of the bar back into the belly's sides,
#     its contents dim;
#   - pelican_digesting.json, the same while it digests: its contents lit and glowing, and drips
#     glowing down its neck and arms.
# PelicanBlock's shape follows the same boxes.
import json
import shutil
import sys

from PIL import Image

OUT = 'src/main/resources/assets/mechanicalmasteryreborn/textures/block/'
MODELS = 'src/main/resources/assets/mechanicalmasteryreborn/models/block/'
TEXTURE = 'mechanicalmasteryreborn:block/'
VITAL = (101, 219, 101)  # the Vital Port's colour (NOTICE)
GLASS = (205, 230, 235, 56)  # the faint glass between the frame's lines
DIM = 0.45  # how bright the contents are while the Pelican eats
DRIP_ROWS = range(5, 12)  # the dripping texture's drops, below its blob


def lightness(pixel):
    r, g, b, _ = pixel
    return (max(r, g, b) + min(r, g, b)) / 510


def recolour(image, colour, brightness):
    """Each pixel the colour scaled by 0.3 plus its lightness, as the mint's molten metals, times brightness."""
    out = image.copy()
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            pixel = pixels[x, y]
            scale = (0.3 + lightness(pixel)) * brightness
            pixels[x, y] = tuple(min(255, round(c * scale)) for c in colour) + (pixel[3],)
    return out


def glass(source):
    out = source.copy()
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            if pixels[x, y][3] == 0:
                pixels[x, y] = GLASS
    return out


def drip(source, frames=16):
    """The drops alone, falling a pixel a frame down a 16-pixel strip of frames."""
    drops = [(x, y) for y in DRIP_ROWS for x in range(16) if source.getpixel((x, y))[3] > 0]
    strip = Image.new('RGBA', (16, 16 * frames))
    for frame in range(frames):
        for x, y in drops:
            strip.putpixel((x, frame * 16 + (y + frame) % 16), VITAL + (255,))
    return strip


def element(start, end, texture, uvs, light=False):
    """A box from start to end, each face of uvs drawn from texture; one resting on the ground culls its bottom."""
    faces = {side: {'texture': texture, 'uv': uv} for side, uv in uvs.items()}
    if 'down' in faces and start[1] == 0:
        faces['down']['cullface'] = 'down'
    box = {'from': start, 'to': end, 'faces': faces}
    if light:
        box['light_emission'] = 15
    return box


def around(u0, v0, u1, v1, **more):
    """The same uv on its four sides, and any more faces given."""
    return {side: [u0, v0, u1, v1] for side in ('north', 'south', 'east', 'west')} | more


def model(lit):
    boxes = [
        element([2, 0, 2], [14, 3, 14], '#bronze', around(2, 13, 14, 16, up=[2, 2, 14, 14], down=[2, 2, 14, 14])),
        element([3.5, 3, 3.5], [12.5, 9, 12.5], '#contents', around(3.5, 7, 12.5, 13, up=[3.5, 3.5, 12.5, 12.5]), light=lit),
        element([3, 3, 3], [13, 11, 13], '#glass', around(0, 0, 16, 16, up=[0, 0, 16, 16])),
        element([4, 11, 4], [12, 12, 12], '#glass', around(4, 0, 12, 1, up=[4, 4, 12, 12])),
        element([7, 12, 7], [9, 14, 9], '#glass', around(7, 0, 9, 2)),
        element([1, 14, 7], [15, 16, 9], '#glass', {'north': [1, 0, 15, 2], 'south': [1, 0, 15, 2], 'east': [7, 0, 9, 2],
                                                      'west': [7, 0, 9, 2], 'up': [1, 7, 15, 9], 'down': [1, 7, 15, 9]}),
    ]
    for x in (1, 13):
        boxes.append(element([x, 6, 7], [x + 2, 14, 9], '#glass', around(x, 2, x + 2, 10, down=[x, 7, x + 2, 9])))
        if lit:
            boxes.append(element([x + 0.5, 6, 7.5], [x + 1.5, 15.5, 8.5], '#drip', around(6, 0, 8, 16), light=True))
    if lit:
        boxes.append(element([7.5, 12, 7.5], [8.5, 15.5, 8.5], '#drip', around(8, 0, 9, 16), light=True))
    textures = {'particle': TEXTURE + 'pelican_bronze', 'bronze': TEXTURE + 'pelican_bronze', 'glass': TEXTURE + 'pelican_glass',
                'contents': TEXTURE + ('pelican_contents_lit' if lit else 'pelican_contents')}
    if lit:
        textures['drip'] = TEXTURE + 'pelican_drip'
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:translucent', 'textures': textures, 'elements': boxes}


def write_json(value, path):
    with open(path, 'w') as out:
        json.dump(value, out, indent=2)
        out.write('\n')


def main(textures):
    blocks, fluids, items = textures + '/blocks/', textures + '/fluids/', textures + '/items/'
    shutil.copyfile(blocks + 'block_metal_black_bronze_block_1.png', OUT + 'pelican_bronze.png')
    glass(Image.open(blocks + 'glass_crystal_0.png').convert('RGBA')).save(OUT + 'pelican_glass.png')
    molten = Image.open(fluids + 'fluid_molten.png').convert('RGBA')
    recolour(molten.crop((0, 0, 16, 16)), VITAL, DIM).save(OUT + 'pelican_contents.png')
    recolour(molten, VITAL, 1).save(OUT + 'pelican_contents_lit.png')
    shutil.copyfile(fluids + 'fluid_molten.png.mcmeta', OUT + 'pelican_contents_lit.png.mcmeta')
    drip(Image.open(items + 'alchemy_pot_contents_dripping.png').convert('RGBA')).save(OUT + 'pelican_drip.png')
    with open(OUT + 'pelican_drip.png.mcmeta', 'w') as mcmeta:
        json.dump({'animation': {'frametime': 2}}, mcmeta, indent='\t')
        mcmeta.write('\n')
    write_json(model(False), MODELS + 'pelican.json')
    write_json(model(True), MODELS + 'pelican_digesting.json')


if __name__ == '__main__':
    main(sys.argv[1])
