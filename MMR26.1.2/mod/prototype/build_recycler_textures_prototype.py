#!/usr/bin/env python3
# PROTOTYPE, throwaway (#166): builds recycler-textures.prototype.html, the Recycler's parts and its whole 3x3
# in 3D with candidate textures from ~/minecraft_mods/unused-textures (CC BY 4.0, Malcolm Riley), embedded.
#   python3 build_recycler_textures_prototype.py && xdg-open recycler-textures.prototype.html
import base64, json, pathlib
SRC = pathlib.Path.home() / "minecraft_mods/unused-textures"
B = lambda n: SRC / "blocks" / f"{n}.png"
I = lambda n: SRC / "items" / f"{n}.png"

# Each variant: a texture per face role. Faces: front (+z, the core's front / the niche's open side), side, top, bottom.
# The chosen set, every face (the drive keeps no turning face, so a turn shows nothing). The variants try the
# drive's top completes the outer ring of rails the outputs' and the input's tops draw: an L along its outer edges,
# turned to its corner. The edge texture's two rails are alike, so the input and outputs may turn either way.
BASE = {
    "core": {"front": "space-plating-convex-rivets-horizontal", "side": "space-plating-convex-rivets-horizontal", "top": "space-plating-vent", "bottom": "space-plating-vent"},
    "input": {"top": "derived:edge", "front": "space_crate_top", "bottom": "derived:edge"},
    "output": {"frame": "space_crate_sides", "back": "space_crate_top", "cap": "derived:edge", "niche": "space-plating-plain", "rim": "derived:rim"},
    "casing": {"side": "space-plating-convex-rivets-horizontal", "top": "derived:edge", "bottom": "derived:edge"}}
drive = lambda top, corner=False: {"side": "space-plating-square", "top": top, "bottom": top + "-bottom" if corner else "derived:edge", "turning": "space-plating-square", "corner": corner}
VARIANTS = {
  "A": {"name": "Chosen, corner L", **BASE, "drive": drive("derived:corner", True)},
}
ITEMS = ["annealed_copper_cog", "annealed_copper_plate", "ingot_bronze"]

def uri(path):
    # An animated texture is a strip of frames; the prototype shows its first.
    from io import BytesIO
    from PIL import Image
    im = Image.open(path)
    if im.height > im.width:
        im = im.crop((0, 0, im.width, im.width))
    out = BytesIO(); im.save(out, "PNG")
    return "data:image/png;base64," + base64.b64encode(out.getvalue()).decode()

def derived(kind):
    # Rails redrawn from one plain stretch of the convex-inset-horizontal texture (column 8), with the knob at
    # column 2 drawn the same at both ends, so every rail of the ring is the same pixels: the original's rails end
    # unevenly (its dark line turns light in the last column, and its two knobs differ), which steps where a turned
    # output meets a drive. "edge" is the whole texture with both rails redrawn, for the output's cap and the
    # input's and casing's tops; "corner" is the north-west drive's top, the outer rail along its north and west edges,
    # mitred where they meet.
    from io import BytesIO
    from PIL import Image
    plain = Image.open(B("space-plating-plain")).convert("RGBA")
    horizontal = Image.open(B("space-plating-convex-inset-horizontal")).convert("RGBA")
    def strip(rows, knobs):
        out = Image.new("RGBA", (16, len(rows)))
        for x in range(16):
            for i, y in enumerate(rows):
                out.putpixel((x, i), horizontal.getpixel((2 if x in knobs else 8, y)))
        return out
    if kind == "rim":
        # The output's front: plain plating with the drive's bevel, its outermost rows and columns, and no bolts.
        square = Image.open(B("space-plating-square")).convert("RGBA")
        out = plain.copy()
        for i in range(16):
            for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
                out.putpixel((x, y), square.getpixel((x, y)))
        # A darker lip round the opening (x 1-14, y 4-11), in the shade of the drive's bolt shadows: the rows just
        # above and below it, and the 1px side strips beside it, where the lip takes the bevel's place.
        lip = square.getpixel((2, 3))
        for x in range(0, 16):
            out.putpixel((x, 3), lip)
            out.putpixel((x, 12), lip)
        for y in range(3, 13):
            out.putpixel((0, y), lip)
            out.putpixel((15, y), lip)
    elif kind == "edge":
        # Both rails the outer one, the inner mirrored, so the texture reads the same whichever way its block turns.
        out = horizontal.copy()
        outer = strip(range(0, 4), (2, 13))
        out.paste(outer, (0, 0))
        out.paste(outer.transpose(Image.Transpose.FLIP_TOP_BOTTOM), (0, 12))
    else:
        # Mitred: a pixel within 4 of the north or west edge takes the rail's shade (light, dark, light, shadow) by
        # its distance from the nearer one, so the rail turns the corner as a frame does instead of crossing itself.
        out = plain.copy()
        for y in range(16):
            for x in range(16):
                depth = min(x, y)
                if depth < 4:
                    out.putpixel((x, y), horizontal.getpixel((8, depth)))
    if kind == "corner-bottom":
        out = out.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    buffer = BytesIO(); out.save(buffer, "PNG")
    out.save(pathlib.Path(__file__).parent / f"derived-{kind}.png")
    return "data:image/png;base64," + base64.b64encode(buffer.getvalue()).decode()

textures = {"derived:corner": derived("corner"), "derived:corner-bottom": derived("corner-bottom"), "derived:edge": derived("edge"), "derived:rim": derived("rim")}
for v in VARIANTS.values():
    for part, faces in v.items():
        if isinstance(faces, dict):
            for name in faces.values():
                if isinstance(name, str) and not name.startswith("derived:"):
                    textures[name] = uri(B(name))
for name in ITEMS:
    textures["item:" + name] = uri(I(name))

html = (pathlib.Path(__file__).parent / "recycler-textures.template.html").read_text()
html = html.replace("/*DATA*/", "const VARIANTS = " + json.dumps(VARIANTS) + ";\nconst TEXTURES = " + json.dumps(textures)
                    + ";\nconst ITEMS = " + json.dumps(["item:" + n for n in ITEMS]) + ";")
out = pathlib.Path(__file__).parent / "recycler-textures.prototype.html"
out.write_text(html)
print(out)
