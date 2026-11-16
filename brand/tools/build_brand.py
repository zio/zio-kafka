"""Build the ZIO Kafka brand kit: SVG sources, PNG exports, social cards and brand-sheet boards.

Usage (fontTools and uharfbuzz are the Python dependencies; Chromium does the rasterising):
    uv run --with fonttools --with uharfbuzz python tools/build_brand.py \
        <Sora[wght].ttf> <JetBrainsMono[wght].ttf> <brand dir> <boards dir>

<boards dir> holds the canvas boards (Brand*.dc.html). Banners, social cards and the talk slide are cut
out of boards 7 and 8 at full size; all boards are copied to sheet/ and rendered to PNG.
"""
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile

import uharfbuzz as hb
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

SANS, MONO, OUT, BOARDS = (os.path.abspath(p) for p in sys.argv[1:5])
CHROME = shutil.which("chromium") or shutil.which("google-chrome-stable") or shutil.which("chromium-browser")

# ---------------------------------------------------------------- palette (board 3)
INK, SNOW, PAPER, CHARCOAL = "#101521", "#F4F5F8", "#FFFFFF", "#2A3245"
ORANGE, ORANGE_400 = "#E8590C", "#FF8A4C"

# ---------------------------------------------------------------- the ring Z (120-unit grid)
LINKS = "M32.5 22H49.5M70.5 22H87.5M90.58 29.42L70.61 49.39M49.39 70.61L29.42 90.58M32.5 98H49.5M70.5 98H87.5"
RINGS = "".join(f'<circle cx="{x}" cy="{y}" r="10.5"/>' for y in (22, 98) for x in (22, 60, 98))


def ring_z(rings, hub, sw=10, hub_sw=11):
    return (f'<g fill="none" stroke="{rings}" stroke-width="{sw}"><path d="{LINKS}"/>{RINGS}</g>'
            f'<circle cx="60" cy="60" r="15" fill="none" stroke="{hub}" stroke-width="{hub_sw}"/>')


def scaled(inner, s):
    return f'<g transform="translate(60 60) scale({s}) translate(-60 -60)">{inner}</g>'


def tile(bg, hub, rx=26, s=0.74):
    shape = f'<rect width="120" height="120" rx="{rx}" fill="{bg}"/>' if rx else f'<rect width="120" height="120" fill="{bg}"/>'
    return shape + scaled(ring_z(PAPER, hub, 11, 12), s)


def svg(w, h, vb, body, title=None):
    t = f"<title>{title}</title>" if title else ""
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="{vb}">{t}{body}</svg>\n'


# ---------------------------------------------------------------- wordmark outlines (HarfBuzz shaping, kerning on)
class Face:
    def __init__(self, path, wght):
        self.tt = instantiateVariableFont(TTFont(path), {"wght": wght})
        blob = hb.Blob.from_file_path(path)
        self.hb = hb.Font(hb.Face(blob))
        self.hb.set_variations({"wght": wght})
        self.upm = self.tt["head"].unitsPerEm
        self.gs = self.tt.getGlyphSet()
        self.order = self.tt.getGlyphOrder()
        hhea = self.tt["hhea"]
        self.ascent, self.descent = hhea.ascent / self.upm, -hhea.descent / self.upm

    def outline(self, text, size, x, y, tracking_em):
        """Path data for text set at (x, baseline y); CSS letter-spacing after every glyph. Returns (d, x_end)."""
        buf = hb.Buffer()
        buf.add_str(text)
        buf.guess_segment_properties()
        hb.shape(self.hb, buf, {"kern": True, "liga": False, "calt": False})
        sc = size / self.upm
        pen = SVGPathPen(self.gs)
        cx = x
        for info, pos in zip(buf.glyph_infos, buf.glyph_positions):
            name = self.order[info.codepoint]
            self.gs[name].draw(TransformPen(pen, (sc, 0, 0, -sc, cx + pos.x_offset * sc, y - pos.y_offset * sc)))
            cx += pos.x_advance * sc + tracking_em * size
        return pen.getCommands(), cx


SORA = Face(SANS, 600)
JBM = Face(MONO, 500)
EM = 100.0


def z_mark(x, base, rings, hub):
    """Board lockup: the mark is 0.82 em square, its box 0.045 em below the baseline."""
    k = 0.82 * EM / 120
    top = base + 0.045 * EM - 0.82 * EM
    return f'<g transform="translate({x:.3f} {top:.3f}) scale({k:.5f})">{ring_z(rings, hub)}</g>', x + 0.82 * EM


def logo(text, hub, stacked=False):
    parts = []
    base = 100.0
    m, x = z_mark(0, base, text, hub)
    parts.append(m)
    x += 0.02 * EM
    d_io, x = JBM.outline("IO", EM, x, base, -0.04)
    parts.append(f'<path d="{d_io}" fill="{text}"/>')
    if stacked:
        # Each line is a line-height: 1 box, 0.14 em apart; the baseline sits where the font's half-leading puts it.
        line1_top = base - (JBM.ascent - (JBM.ascent + JBM.descent - 1) / 2) * EM
        line2_top = line1_top + 1.14 * EM
        base2 = line2_top + (SORA.ascent - (SORA.ascent + SORA.descent - 1) / 2) * EM
        d_k, x2 = SORA.outline("Kafka", EM, 0, base2, -0.025)
        right, bottom = max(x, x2), base2 + 0.22 * EM
    else:
        x += 0.28 * EM
        d_k, x = SORA.outline("Kafka", EM, x, base, -0.025)
        right, bottom = x, base + 0.22 * EM
    parts.append(f'<path d="{d_k}" fill="{text}"/>')
    top = base - 0.82 * EM - 6
    pad = 6
    vb_w, vb_h = right + 2 * pad, bottom - top
    h = 240 if stacked else 120
    w = round(h * vb_w / vb_h)
    return svg(w, h, f"{-pad} {top:.2f} {vb_w:.2f} {vb_h:.2f}", "".join(parts), "ZIO Kafka")


# ---------------------------------------------------------------- write SVG sources
def write(name, content):
    with open(os.path.join(OUT, name), "w") as f:
        f.write(content)


os.makedirs(OUT, exist_ok=True)
V = "0 0 120 120"
write("zio-kafka-logo-light.svg", logo(INK, ORANGE))
write("zio-kafka-logo-dark.svg", logo(SNOW, ORANGE_400))
write("zio-kafka-logo-stacked-light.svg", logo(INK, ORANGE, stacked=True))
write("zio-kafka-logo-stacked-dark.svg", logo(SNOW, ORANGE_400, stacked=True))
write("zio-kafka-mark-light.svg", svg(240, 240, V, ring_z(INK, ORANGE), "ZIO Kafka mark"))
write("zio-kafka-mark-dark.svg", svg(240, 240, V, ring_z(SNOW, ORANGE_400), "ZIO Kafka mark"))
write("zio-kafka-mark-mono.svg", svg(240, 240, V, ring_z("currentColor", "currentColor"), "ZIO Kafka mark"))
write("zio-kafka-symbol.svg", svg(240, 240, V, tile(INK, ORANGE), "ZIO Kafka"))
write("zio-kafka-symbol-charcoal.svg", svg(240, 240, V, tile(CHARCOAL, ORANGE_400), "ZIO Kafka"))
write("favicon.svg", svg(16, 16, V, tile(INK, ORANGE)))
write("apple-touch-icon.svg", svg(180, 180, V, tile(INK, ORANGE, rx=0)))
write("icon-maskable.svg", svg(512, 512, V, tile(INK, ORANGE, rx=0, s=0.6)))

with open(os.path.join(OUT, "tokens.css"), "w") as f:
    f.write("""/* ZIO Kafka brand tokens. Light by default, dark through prefers-color-scheme. */
:root {
  --zk-bg: #FFFFFF;           /* paper */
  --zk-surface: #F4F5F8;      /* snow: panels, code */
  --zk-line: #E3E6EC;         /* borders, never text */
  --zk-text: #101521;         /* ink */
  --zk-text-2: #566074;       /* slate */
  --zk-accent: #E8590C;       /* signal orange: hub, buttons, 24 px+ type (3.58:1 on white) */
  --zk-link: #C2410C;         /* orange 700: orange as text (5.18:1) */
  --zk-on-accent: #101521;    /* labels on orange (5.09:1) */
  --zk-tile: #101521;         /* symbol tile */
  --zk-tint: #FFD3B8;         /* peach: fills, highlights */
  --zk-code-keyword: #C2410C;
  --zk-code-string: #7C2D12;
  --zk-font-sans: "Sora", system-ui, sans-serif;
  --zk-font-mono: "JetBrains Mono", ui-monospace, monospace;
  --zk-radius-tile: 22%;      /* 26 of 120 units */
}
@media (prefers-color-scheme: dark) {
  :root {
    --zk-bg: #101521;         /* ink */
    --zk-surface: #171C28;    /* ink 800 */
    --zk-line: #232833;       /* ink 700 */
    --zk-text: #F4F5F8;       /* snow */
    --zk-text-2: #A7AEBD;     /* mist */
    --zk-accent: #FF8A4C;     /* orange 400 (7.81:1 on ink) */
    --zk-link: #FF8A4C;
    --zk-on-accent: #101521;
    --zk-tile: #2A3245;       /* charcoal */
    --zk-code-keyword: #FF8A4C;
    --zk-code-string: #FFD3B8; /* peach */
  }
}
""")

if not CHROME:
    sys.exit("Chromium not found: SVG sources written, PNGs skipped.")

# ---------------------------------------------------------------- rasterising
TMP = tempfile.mkdtemp(prefix="zk-brand-")
FONTFACE = (f"@font-face{{font-family:'Sora';src:url('file://{SANS}') format('truetype');font-weight:100 800}}"
            f"@font-face{{font-family:'JetBrains Mono';src:url('file://{MONO}') format('truetype');font-weight:100 800}}")


def shoot(html, w, h, out, scale=1, transparent=False):
    p = os.path.join(TMP, "p.html")
    with open(p, "w") as f:
        f.write(html)
    args = [CHROME, "--headless", "--no-sandbox", "--disable-gpu", "--hide-scrollbars", "--allow-file-access-from-files",
            f"--window-size={w},{h}", f"--force-device-scale-factor={scale}", f"--screenshot={out}",
            "--virtual-time-budget=4000"]
    if transparent:
        args.append("--default-background-color=00000000")
    subprocess.run(args + ["file://" + p], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def shoot_svg(src, size, out):
    shoot(f"<body style='margin:0;background:transparent'><img src='file://{os.path.join(OUT, src)}' width={size} height={size} "
          f"style='display:block'>", size, size, os.path.join(OUT, out), transparent=True)


for src, size, out in [("favicon.svg", 16, "favicon-16.png"), ("favicon.svg", 32, "favicon-32.png"),
                       ("favicon.svg", 48, "favicon-48.png"), ("apple-touch-icon.svg", 180, "apple-touch-icon.png"),
                       ("zio-kafka-symbol.svg", 192, "icon-192.png"), ("zio-kafka-symbol.svg", 512, "icon-512.png"),
                       ("icon-maskable.svg", 512, "icon-maskable-512.png"), ("icon-maskable.svg", 500, "avatar-500.png")]:
    shoot_svg(src, size, out)
os.remove(os.path.join(OUT, "apple-touch-icon.svg"))

pngs = [open(os.path.join(OUT, f"favicon-{n}.png"), "rb").read() for n in (16, 32, 48)]
off, ents, data = 6 + 16 * 3, b"", b""
for n, p in zip((16, 32, 48), pngs):
    ents += struct.pack("<BBBBHHII", n, n, 0, 0, 1, 32, len(p), off + len(data))
    data += p
with open(os.path.join(OUT, "favicon.ico"), "wb") as f:
    f.write(struct.pack("<HHH", 0, 1, 3) + ents + data)


# ---------------------------------------------------------------- boards: cut-outs and sheet
def read_board(name):
    return open(os.path.join(BOARDS, f"{name}.dc.html")).read()


def defs_of(s):
    return re.search(r'<svg width="0" height="0".*?</defs></svg>', s, re.S).group(0)


def element(s, start):
    depth = 0
    for t in re.finditer(r"<div\b|</div>", s[start:]):
        depth += 1 if t.group(0) == "<div" else -1
        if depth == 0:
            return s[start:start + t.end()]
    raise ValueError("unbalanced div")


def cutouts(s, w, h):
    """Full-size artboards on a board: <div style="width: Wpx; height: Hpx; ... background: ..."> in document order."""
    return [element(s, m.start()) for m in re.finditer(rf'<div style="width: {w}px; height: {h}px;[^"]*background:', s)]


def flat(el):
    """Drop the board's preview frame (rounded corners, border) so the export is a plain rectangle."""
    head, rest = el.split(">", 1)
    head = re.sub(r"border-radius: \d+px; ?", "", head)
    head = re.sub(r"border: 1px solid #[0-9A-Fa-f]{6}; ?", "", head)
    return head + ">" + rest


def page(defs, body):
    return (f'<!doctype html><html><head><meta charset="utf-8"><style>{FONTFACE}body{{margin:0;font-family:\'Sora\',sans-serif}}'
            f'</style></head><body>{defs}{body}</body></html>')


hero = read_board("Brand7Hero")
light, dark = cutouts(hero, 1200, 300)[:2]
for name, el in [("readme-banner-light.png", light), ("readme-banner-dark.png", dark)]:
    shoot(page(defs_of(hero), flat(el)), 1200, 300, os.path.join(OUT, name), scale=2)

social = read_board("Brand8Social")
d8 = defs_of(social)
gh, og, rel, sl = cutouts(social, 1280, 640), cutouts(social, 1200, 630), None, cutouts(social, 1920, 1080)
og, rel = og[:2], og[2:4]
for name, el, w, h in [("social-github-light.png", gh[0], 1280, 640), ("social-github-dark.png", gh[1], 1280, 640),
                       ("social-og-light.png", og[0], 1200, 630), ("social-og-dark.png", og[1], 1200, 630),
                       ("release-card-major.png", rel[0], 1200, 630), ("release-card-minor.png", rel[1], 1200, 630),
                       ("slide-title.png", sl[0], 1920, 1080)]:
    shoot(page(d8, el), w, h, os.path.join(OUT, name), scale=2)


def board_to_html(src, local_fonts=True):
    s = open(src).read()
    helmet = re.search(r"<helmet>(.*?)</helmet>", s, re.S).group(1)
    if local_fonts:
        helmet = f"<style>{FONTFACE}</style>" + re.sub(r'<link[^>]+fonts\.google[^>]*>', "", helmet)
    body = re.search(r"<x-dc>(.*?)</x-dc>", s, re.S).group(1)
    body = re.sub(r"<helmet>.*?</helmet>", "", body, flags=re.S)
    w, h = (int(x) for x in re.search(r'"\$preview":\{"width":(\d+),"height":(\d+)\}', s).groups())
    title = re.search(r"<title>(.*?)</title>", s).group(1)
    return (f'<!doctype html><html lang="en"><head><meta charset="utf-8"><title>{title}</title>{helmet}</head>'
            f'<body>{body}</body></html>'), w, h


sheet = os.path.join(OUT, "sheet")
os.makedirs(sheet, exist_ok=True)
for name in sorted((f[:-8] for f in os.listdir(BOARDS) if re.match(r"Brand\d+\w*\.dc\.html$", f)),
                   key=lambda n: int(re.match(r"Brand(\d+)", n).group(1))):
    src = os.path.join(BOARDS, f"{name}.dc.html")
    if name.startswith("Brand0"):
        html, w, h = board_to_html(src)
        shoot(html, w, h, os.path.join(OUT, "brand-sheet.png"))
        with open(os.path.join(OUT, "brand-sheet.html"), "w") as f:
            f.write(board_to_html(src, local_fonts=False)[0])
        continue
    shutil.copy(src, os.path.join(sheet, f"{name}.dc.html"))
    html, w, h = board_to_html(src)
    shoot(html, w, h, os.path.join(sheet, f"{name}.png"))

shutil.rmtree(TMP, ignore_errors=True)
print("done:", OUT)
