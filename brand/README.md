# ZIO Kafka brand

Final identity: the Z of ZIO drawn as linked rings. Six rings sit on a three-column grid, top row and bottom row, joined by short links along the rows and two diagonals, with a bigger hub ring in the centre in signal orange. Rings are records and partitions, links are the flow, the hub is the consumer. It nods loosely to Apache Kafka's ring-and-spoke logo without tracing it. In the full logo the ring Z is the Z of "ZIO", followed by "IO Kafka" in Sora: one typeface for the whole name, so the ring Z alone marks the ZIO part.

## Files

**Logo and mark**
- `zio-kafka-logo-light.svg`, `zio-kafka-logo-dark.svg`: full logo, horizontal (the default). The ring Z at 0.82 em, "IO" in Sora 600, a 0.24 em space, "Kafka" in Sora 600, both at −2.5% tracking. Text converted to outlines.
- `zio-kafka-logo-stacked-light.svg`, `zio-kafka-logo-stacked-dark.svg`: "ZIO" over "Kafka", for square spaces and covers.
- `zio-kafka-mark-light.svg`, `zio-kafka-mark-dark.svg`: the bare ring Z, for building a lockup in live type.
- `zio-kafka-mark-mono.svg`: one colour (`currentColor`), for stamps, engraving and single-ink print.
- `zio-kafka-symbol.svg`: the ink tile with white rings and an orange hub, the default symbol.
- `zio-kafka-symbol-charcoal.svg`: the charcoal tile, for dark grounds where the ink tile would disappear.

**Icons**
- `favicon.svg`: the browser tab icon, the symbol tile itself. One file for light and dark tabs.
- `favicon.ico` (16, 32, 48), `favicon-16.png`, `favicon-32.png`, `favicon-48.png`.
- `apple-touch-icon.png` (180): square corners, iOS rounds them.
- `icon-192.png`, `icon-512.png`: the rounded tile, for the web app manifest.
- `icon-maskable.svg`, `icon-maskable-512.png`: full-bleed square, mark at 60% so the rings stay inside Android's safe circle.
- `avatar-500.png`: the full-bleed square for GitHub, Slack, X and Discord, which crop it to a circle or rounded square.

**Cards and banners** (PNG at 2×, light and dark)
- `social-github-light.png`, `social-github-dark.png`: 1280 × 640, repository Settings › Social preview.
- `social-og-light.png`, `social-og-dark.png`: 1200 × 630, `og:image` and `twitter:image` (LinkedIn, Slack, Discord, X).
- `readme-banner-light.png`, `readme-banner-dark.png`: 1200 × 300, top of the README, switched with `<picture>`.
- `release-card-major.png`, `release-card-minor.png`: 1200 × 630 release posts. The version and highlights are placeholders.
- `slide-title.png`: 1920 × 1080 talk title slide. The title, speaker and event are placeholders.

**Tokens and sheet**
- `tokens.css`: every colour, font and radius as `--zk-*` CSS variables, light by default and dark through `prefers-color-scheme`.
- `brand-sheet.html`, `brand-sheet.png`: the one-page overview.
- `sheet/`: the ten brand-sheet sections (logo, icons, colour, type, voice, imagery, hero, social, browser, assets) as canvas boards (`.dc.html`) and PNG.

## Palette

| Role | Light | Dark |
|---|---|---|
| Signal orange: the hub, buttons, large type | `#E8590C` (3.58:1 on white: shapes and 24 px+ type only) | `#FF8A4C` (7.81:1 on ink) |
| Orange as text: links, code keywords | `#C2410C` (5.18:1) | `#FF8A4C` |
| Text on orange | Ink `#101521` (5.09:1), never white | Ink `#101521` |
| Text / page | Ink `#101521` on Paper `#FFFFFF` | Snow `#F4F5F8` on Ink `#101521` |
| Secondary text, panel, line | `#566074`, `#F4F5F8`, `#E3E6EC` | `#A7AEBD`, `#171C28`, `#232833` |
| Symbol tile | Ink `#101521` | Charcoal `#2A3245` |

Orange belongs in the hub and in one accent per layout, not as a large field behind the mark. In the ZIO family, Config is orange-red `#F2501F`, Prelude terracotta, Logging amber, Schema violet, JSON raspberry and HTTP green; ZIO Kafka's signal orange is the yellower, ink-paired neighbour of Config's.

## Docs site `<head>`

```html
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="icon" href="/favicon.ico" sizes="32x32">
<link rel="apple-touch-icon" href="/apple-touch-icon.png">
<link rel="manifest" href="/site.webmanifest">
<meta name="theme-color" content="#FFFFFF" media="(prefers-color-scheme: light)">
<meta name="theme-color" content="#101521" media="(prefers-color-scheme: dark)">
```

## Rebuild

Needs `uv`, headless Chromium, and the two open-source fonts from the Google Fonts repository (Sora for the logo and text, JetBrains Mono for code on the boards): `Sora[wght].ttf` (`ofl/sora`) and `JetBrainsMono[wght].ttf` (`ofl/jetbrainsmono`).

```bash
uv run --with fonttools --with uharfbuzz python tools/build_brand.py \
  <Sora[wght].ttf> <JetBrainsMono[wght].ttf> . <dir with Brand*.dc.html boards>
```

The script writes the SVG sources (wordmark shaped with HarfBuzz, kerning on, then outlined) and `tokens.css`, rasterises the icons with Chromium, builds `favicon.ico`, cuts the banners, social cards, release cards and slide out of boards 7 and 8 at full size, and copies the brand-sheet boards to `sheet/` and renders them to PNG.

The tagline "Kafka streams, the ZIO way", the `zio.dev/zio-kafka` address and the sample copy are proposals for the maintainers to confirm.

Apache Kafka and Kafka are trademarks of the Apache Software Foundation. ZIO Kafka is not affiliated with or endorsed by the ASF.
