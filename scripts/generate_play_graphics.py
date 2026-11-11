#!/usr/bin/env python3
"""
FlowPebble Google Play Console Store Listing Graphics Generator
Generates 100% copyright-free, pixel-perfect PNG assets for Google Play Console:
  1. app_icon_512x512.png (512x512, 32-bit RGBA PNG)
  2. feature_graphic_1024x500.png (1024x500, 24-bit RGB PNG, no alpha)
  3. phone_screenshot_1_today_dashboard.png (1080x1920, 9:16 portrait)
  4. phone_screenshot_2_checklist_timer.png (1080x1920, 9:16 portrait)
  5. phone_screenshot_3_create_share.png (1080x1920, 9:16 portrait)
  6. phone_screenshot_4_offline_provault.png (1080x1920, 9:16 portrait)
  7. tablet_7inch_landscape_1920x1200.png (1920x1200, 16:10 landscape)
  8. tablet_10inch_landscape_1920x1200.png (1920x1200, 16:10 landscape)
"""

import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent
OUT_DIR = REPO_ROOT / "play-store-graphics"
OUT_DIR.mkdir(parents=True, exist_ok=True)

# Brand Palette
DARK_BG = (16, 27, 26)           # #101B1A
SURFACE = (24, 38, 36)           # #182624
SURFACE_ELEV = (31, 51, 47)      # #1F332F
FOREST_START = (36, 74, 64)      # #244A40
MINT_ACCENT = (121, 224, 178)    # #79E0B2
MINT_HIGHLIGHT = (165, 242, 205) # #A5F2CD
SKY_ACCENT = (155, 200, 245)     # #9BC8F5
SAGE_MUTED = (155, 174, 170)     # #9BAEAA
TEXT_LIGHT = (242, 251, 247)     # #F2FBF7
BORDER_COLOR = (42, 64, 59)      # #2A403B
LIGHT_BG = (244, 251, 247)       # #F4FBF7
LIGHT_SURFACE = (255, 255, 255)  # #FFFFFF
EMERALD_PRI = (20, 83, 60)       # #14533C
CHARCOAL_INK = (17, 31, 28)      # #111F1C


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = (
        [
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
            "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf",
        ]
        if bold
        else [
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf",
        ]
    )
    for path in candidates:
        if os.path.exists(path):
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def make_vertical_gradient(width: int, height: int, top_rgb, bottom_rgb, mode="RGB") -> Image.Image:
    base = Image.new(mode, (width, height))
    draw = ImageDraw.Draw(base)
    for y in range(height):
        t = y / max(height - 1, 1)
        r = int(top_rgb[0] * (1 - t) + bottom_rgb[0] * t)
        g = int(top_rgb[1] * (1 - t) + bottom_rgb[1] * t)
        b = int(top_rgb[2] * (1 - t) + bottom_rgb[2] * t)
        if mode == "RGBA":
            draw.line([(0, y), (width, y)], fill=(r, g, b, 255))
        else:
            draw.line([(0, y), (width, y)], fill=(r, g, b))
    return base


def draw_flowpebble_emblem(draw: ImageDraw.ImageDraw, cx: int, cy: int, size: int):
    """Draws the signature FlowPebble 'e' + leaf + pebble vector emblem at (cx, cy)."""
    half = size // 2
    x0, y0 = cx - half, cy - half
    x1, y1 = cx + half, cy + half
    radius = int(size * 0.24)

    # Rounded squircle container
    draw.rounded_rectangle([x0, y0, x1, y1], radius=radius, fill=FOREST_START, outline=MINT_ACCENT, width=max(2, size // 96))

    # Inner dark gradient panel
    pad = int(size * 0.06)
    draw.rounded_rectangle([x0 + pad, y0 + pad, x1 - pad, y1 - pad], radius=radius - pad // 2, fill=DARK_BG)

    # Stylized 'E' / Pebble curve in Mint
    s = size / 512.0
    ex0 = int(x0 + 130 * s)
    ey0 = int(y0 + 130 * s)
    ex1 = int(x0 + 382 * s)
    ey1 = int(y0 + 412 * s)
    stroke_w = max(8, int(56 * s))

    # Top bar, middle bar, bottom bar, and left spine
    draw.rounded_rectangle([ex0, ey0, ex1, ey1], radius=int(120 * s), outline=MINT_ACCENT, width=stroke_w)
    # Open right side of C-shape
    draw.rectangle([int(x0 + 310 * s), int(y0 + 188 * s), ex1 + stroke_w, int(y0 + 354 * s)], fill=DARK_BG)
    # Middle horizontal crossbar
    mid_y = int(y0 + 258 * s)
    draw.rounded_rectangle([int(x0 + 148 * s), mid_y, int(x0 + 378 * s), mid_y + int(52 * s)], radius=int(24 * s), fill=MINT_ACCENT)

    # Leaf overlay polygon
    leaf_pts = [
        (int(x0 + 245 * s), int(y0 + 318 * s)),
        (int(x0 + 268 * s), int(y0 + 230 * s)),
        (int(x0 + 335 * s), int(y0 + 192 * s)),
        (int(x0 + 404 * s), int(y0 + 198 * s)),
        (int(x0 + 372 * s), int(y0 + 282 * s)),
        (int(x0 + 308 * s), int(y0 + 322 * s)),
    ]
    draw.polygon(leaf_pts, fill=(232, 255, 244))

    # Two floating pebbles
    p1_x, p1_y, p1_r = int(x0 + 371 * s), int(y0 + 130 * s), max(4, int(17 * s))
    p2_x, p2_y, p2_r = int(x0 + 412 * s), int(y0 + 171 * s), max(3, int(10 * s))
    draw.ellipse([p1_x - p1_r, p1_y - p1_r, p1_x + p1_r, p1_y + p1_r], fill=(232, 255, 244))
    draw.ellipse([p2_x - p2_r, p2_y - p2_r, p2_x + p2_r, p2_y + p2_r], fill=MINT_ACCENT)


def generate_app_icon():
    img = make_vertical_gradient(512, 512, FOREST_START, DARK_BG, mode="RGBA")
    draw = ImageDraw.Draw(img)
    draw_flowpebble_emblem(draw, 256, 256, 480)
    img.save(OUT_DIR / "app_icon_512x512.png", format="PNG")


def generate_feature_graphic():
    # 1024 x 500, 24-bit RGB (no alpha)
    img = make_vertical_gradient(1024, 500, (28, 60, 52), DARK_BG, mode="RGB")
    draw = ImageDraw.Draw(img)

    # Decorative soft circles
    draw.ellipse([680, -80, 1080, 320], outline=BORDER_COLOR, width=2)
    draw.ellipse([740, 140, 1120, 520], outline=BORDER_COLOR, width=2)

    # Left: Emblem
    draw_flowpebble_emblem(draw, 150, 250, 210)

    # Center-Left: Typography
    f_kicker = load_font(16, bold=True)
    f_title = load_font(54, bold=True)
    f_sub = load_font(23, bold=False)
    f_pill = load_font(16, bold=True)

    draw.rounded_rectangle([285, 88, 640, 122], radius=17, fill=FOREST_START, outline=MINT_ACCENT, width=1)
    draw.text((302, 96), "100% OFFLINE-FIRST ANDROID APP", fill=MINT_HIGHLIGHT, font=f_kicker)

    draw.text((285, 140), "FlowPebble", fill=TEXT_LIGHT, font=f_title)
    draw.text((285, 210), "Small Steps, Shared", fill=MINT_ACCENT, font=load_font(32, bold=True))
    draw.text((285, 262), "Discover, build, check off, and share practical\n3-to-5 step micro-guides — 100% offline.", fill=SAGE_MUTED, font=f_sub)

    # Feature pills at bottom
    pills = ["Room SQLite Offline", "Focus Timer", "Material 3 Dark/Light"]
    px = 285
    for p in pills:
        w = int(draw.textlength(p, font=f_pill)) + 32
        draw.rounded_rectangle([px, 356, px + w, 396], radius=20, fill=SURFACE, outline=BORDER_COLOR, width=2)
        draw.text((px + 16, 367), p, fill=MINT_HIGHLIGHT, font=f_pill)
        px += w + 14

    img.save(OUT_DIR / "feature_graphic_1024x500.png", format="PNG")


def draw_phone_frame_header(draw: ImageDraw.ImageDraw, kicker: str, headline: str, sub: str):
    f_kicker = load_font(22, bold=True)
    f_head = load_font(48, bold=True)
    f_sub = load_font(26, bold=False)

    draw.rounded_rectangle([72, 64, 460, 110], radius=23, fill=FOREST_START, outline=MINT_ACCENT, width=2)
    draw.text((96, 75), kicker, fill=MINT_HIGHLIGHT, font=f_kicker)
    draw.text((72, 132), headline, fill=TEXT_LIGHT, font=f_head)
    draw.text((72, 262), sub, fill=SAGE_MUTED, font=f_sub)

    # Phone mockup bezel container
    draw.rounded_rectangle([68, 340, 1012, 1848], radius=54, fill=DARK_BG, outline=MINT_ACCENT, width=4)
    # Top status + app bar inside phone
    draw.text((112, 378), "09:41", fill=SAGE_MUTED, font=load_font(22, bold=True))
    draw.text((880, 378), "100%", fill=MINT_ACCENT, font=load_font(22, bold=True))


def generate_phone_screenshots():
    # Screenshot 1: Today Dashboard & Daily Micro-Wins
    img1 = make_vertical_gradient(1080, 1920, (26, 56, 48), DARK_BG, mode="RGB")
    d1 = ImageDraw.Draw(img1)
    draw_phone_frame_header(
        d1,
        "FLOWPEBBLE  •  TODAY",
        "Your Next Small Win,\nAlways Available Offline",
        "Track completed micro-guides and jump into actionable 4-step checklists."
    )
    # AppBar
    d1.text((112, 430), "Your next small win", fill=TEXT_LIGHT, font=load_font(36, bold=True))
    d1.text((112, 478), "FLOWPEBBLE  •  SMALL STEPS, SHARED", fill=SAGE_MUTED, font=load_font(18, bold=True))
    # Hero Card
    d1.rounded_rectangle([108, 524, 972, 850], radius=36, fill=FOREST_START, outline=MINT_ACCENT, width=2)
    d1.text((148, 558), "ONE SMALL WIN", fill=MINT_HIGHLIGHT, font=load_font(20, bold=True))
    d1.text((148, 596), "Useful knowledge\ntravels further.", fill=TEXT_LIGHT, font=load_font(44, bold=True))
    d1.text((148, 720), "Save a practical guide. Make your own. Share it anytime.", fill=TEXT_LIGHT, font=load_font(22))
    d1.rounded_rectangle([148, 772, 470, 822], radius=25, fill=DARK_BG)
    d1.text((172, 784), "✓  6 guides completed", fill=MINT_ACCENT, font=load_font(22, bold=True))

    # Guide cards
    cards = [
        ("Review your phone storage safely", "Phone & tech  •  4 steps", "Find large files before deleting anything important."),
        ("Set up a 15-minute focus session", "Study & work  •  4 steps  •  15m Timer", "Turn one vague task into a short, clear 15-minute sprint."),
        ("Reset a cluttered desk in five minutes", "Everyday  •  4 steps  •  5m Timer", "A rapid 5-minute physical reset for a calmer workspace."),
    ]
    cy = 890
    for title, meta, desc in cards:
        d1.rounded_rectangle([108, cy, 972, cy + 220], radius=30, fill=SURFACE, outline=BORDER_COLOR, width=2)
        d1.ellipse([138, cy + 36, 214, cy + 112], fill=FOREST_START)
        d1.text((164, cy + 54), "✓", fill=MINT_ACCENT, font=load_font(34, bold=True))
        d1.text((240, cy + 34), title, fill=TEXT_LIGHT, font=load_font(28, bold=True))
        d1.text((240, cy + 78), meta, fill=MINT_ACCENT, font=load_font(21, bold=True))
        d1.text((240, cy + 118), desc, fill=SAGE_MUTED, font=load_font(22))
        cy += 244

    # Bottom Nav bar
    d1.rounded_rectangle([108, 1690, 972, 1800], radius=28, fill=SURFACE, outline=BORDER_COLOR, width=2)
    d1.text((170, 1730), "● Today          Explore          Saved          Settings", fill=MINT_ACCENT, font=load_font(24, bold=True))
    img1.save(OUT_DIR / "phone_screenshot_1_today_dashboard.png", format="PNG")

    # Screenshot 2: Interactive 3–5 Step Checklists & Built-in Focus Timer
    img2 = make_vertical_gradient(1080, 1920, (26, 56, 48), DARK_BG, mode="RGB")
    d2 = ImageDraw.Draw(img2)
    draw_phone_frame_header(
        d2,
        "INTERACTIVE CHECKLIST + TIMER",
        "Check Off Small Steps &\nStay Focused with Timers",
        "Every step updates your progress bar in real time with tactile haptic feedback."
    )
    d2.text((112, 434), "Set up a 15-minute focus session", fill=TEXT_LIGHT, font=load_font(36, bold=True))
    d2.text((112, 486), "Study & work  •  4 steps", fill=MINT_ACCENT, font=load_font(22, bold=True))

    # Timer Card
    d2.rounded_rectangle([108, 540, 972, 740], radius=30, fill=SURFACE, outline=MINT_ACCENT, width=2)
    d2.text((144, 568), "BUILT-IN FOCUS TIMER", fill=MINT_HIGHLIGHT, font=load_font(20, bold=True))
    d2.text((144, 610), "14:25", fill=TEXT_LIGHT, font=load_font(64, bold=True))
    d2.rounded_rectangle([620, 612, 790, 684], radius=24, fill=FOREST_START)
    d2.text((666, 634), "Pause", fill=MINT_HIGHLIGHT, font=load_font(24, bold=True))
    d2.rounded_rectangle([806, 612, 936, 684], radius=24, fill=DARK_BG, outline=BORDER_COLOR, width=2)
    d2.text((836, 634), "Reset", fill=SAGE_MUTED, font=load_font(24, bold=True))

    # Progress Bar
    d2.text((112, 770), "3 of 4 steps checked (75%)", fill=MINT_ACCENT, font=load_font(22, bold=True))
    d2.rounded_rectangle([108, 810, 972, 832], radius=11, fill=SURFACE)
    d2.rounded_rectangle([108, 810, 756, 832], radius=11, fill=MINT_ACCENT)

    steps = [
        ("1", "Write one concrete outcome you can finish in 15m.", True),
        ("2", "Close unrelated apps and silence notifications.", True),
        ("3", "Start the 15-minute timer and work on that outcome.", True),
        ("4", "Write the single next physical action before stopping.", False),
    ]
    sy = 868
    for num, text, done in steps:
        d2.rounded_rectangle([108, sy, 972, sy + 148], radius=26, fill=SURFACE, outline=MINT_ACCENT if done else BORDER_COLOR, width=2)
        d2.ellipse([136, sy + 44, 196, sy + 104], fill=FOREST_START)
        d2.text((157, sy + 57), num, fill=MINT_ACCENT, font=load_font(26, bold=True))
        d2.text((222, sy + 56), text, fill=SAGE_MUTED if done else TEXT_LIGHT, font=load_font(23, bold=not done))
        d2.rounded_rectangle([892, sy + 50, 942, sy + 100], radius=10, fill=MINT_ACCENT if done else DARK_BG, outline=MINT_ACCENT, width=2)
        if done:
            d2.text((905, sy + 56), "✓", fill=DARK_BG, font=load_font(30, bold=True))
        sy += 168

    d2.rounded_rectangle([108, 1564, 972, 1664], radius=28, fill=MINT_ACCENT)
    d2.text((356, 1598), "✓  Mark as completed", fill=DARK_BG, font=load_font(28, bold=True))
    d2.rounded_rectangle([108, 1688, 972, 1784], radius=28, fill=SURFACE, outline=MINT_ACCENT, width=2)
    d2.text((390, 1720), "Share this guide", fill=MINT_ACCENT, font=load_font(26, bold=True))
    img2.save(OUT_DIR / "phone_screenshot_2_checklist_timer.png", format="PNG")

    # Screenshot 3: Create & Share Your Own Practical Micro-Guides
    img3 = make_vertical_gradient(1080, 1920, (26, 56, 48), DARK_BG, mode="RGB")
    d3 = ImageDraw.Draw(img3)
    draw_phone_frame_header(
        d3,
        "CREATE & SHARE",
        "Build Custom 3–5 Step\nGuides in Seconds",
        "Turn your personal workflows into repeatable checklists and share via Android Share Sheet."
    )
    d3.text((112, 434), "Create a micro-guide", fill=TEXT_LIGHT, font=load_font(36, bold=True))
    d3.text((112, 486), "Capture practical know-how in 3 to 5 repeatable actions.", fill=SAGE_MUTED, font=load_font(22))

    fields = [
        ("Guide title (4–60 chars)", "2-Minute Evening Shutdown Ritual"),
        ("Short summary", "Close open loops so tomorrow starts with zero friction."),
        ("Category", "⚡ Study & work    🌿 Wellness & focus    🪴 Everyday"),
        ("Step 1 (Required)", "Review today's notes and move unfinished items."),
        ("Step 2 (Required)", "Close browser tabs and quit heavy background apps."),
        ("Step 3 (Required)", "Write down the #1 priority task for tomorrow morning."),
        ("Step 4 (Optional)", "Plug in your phone outside the bedroom or on silent."),
    ]
    fy = 548
    for label, val in fields:
        d3.rounded_rectangle([108, fy, 972, fy + 132], radius=22, fill=SURFACE, outline=BORDER_COLOR, width=2)
        d3.text((140, fy + 20), label, fill=MINT_ACCENT, font=load_font(19, bold=True))
        d3.text((140, fy + 58), val, fill=TEXT_LIGHT, font=load_font(24, bold=True))
        fy += 152

    d3.rounded_rectangle([108, 1640, 972, 1752], radius=28, fill=MINT_ACCENT)
    d3.text((380, 1678), "Save Micro-Guide", fill=DARK_BG, font=load_font(30, bold=True))
    img3.save(OUT_DIR / "phone_screenshot_3_create_share.png", format="PNG")

    # Screenshot 4: 100% Offline-First, Dark/Light Material 3 Design & Pro Vault
    img4 = make_vertical_gradient(1080, 1920, (26, 56, 48), DARK_BG, mode="RGB")
    d4 = ImageDraw.Draw(img4)
    draw_phone_frame_header(
        d4,
        "PRO VAULT & MATERIAL 3 THEMES",
        "Emerald Dark & Crisp Light\nModes + Bonus Pro Vault",
        "Switch themes anytime, customize haptics, and unlock bonus deep-work guides."
    )
    # Show split Dark card + Light mode preview card + Pro Vault
    d4.rounded_rectangle([108, 430, 972, 820], radius=32, fill=SURFACE, outline=MINT_ACCENT, width=2)
    d4.text((144, 464), "PRO GUIDE VAULT  •  UNLOCKED", fill=MINT_HIGHLIGHT, font=load_font(21, bold=True))
    d4.text((144, 510), "Deep Work Digital Detox Protocol", fill=TEXT_LIGHT, font=load_font(32, bold=True))
    d4.text((144, 562), "Study & work  •  4 steps  •  25m Built-in Timer", fill=MINT_ACCENT, font=load_font(22, bold=True))
    d4.text((144, 614), "7-Minute Morning Clarity Routine", fill=TEXT_LIGHT, font=load_font(32, bold=True))
    d4.text((144, 666), "Wellness & focus  •  4 steps  •  7m Built-in Timer", fill=MINT_ACCENT, font=load_font(22, bold=True))
    d4.text((144, 728), "Turn a useful tip into a shareable guide", fill=TEXT_LIGHT, font=load_font(30, bold=True))

    # Crisp Alabaster Light Mode Showcase Card
    d4.rounded_rectangle([108, 860, 972, 1340], radius=32, fill=LIGHT_BG, outline=EMERALD_PRI, width=3)
    d4.text((144, 898), "ALABASTER LIGHT MODE PREVIEW", fill=EMERALD_PRI, font=load_font(21, bold=True))
    d4.rounded_rectangle([140, 948, 940, 1120], radius=24, fill=LIGHT_SURFACE, outline=(200, 225, 212), width=2)
    d4.text((172, 976), "Build a reusable packing list", fill=CHARCOAL_INK, font=load_font(30, bold=True))
    d4.text((172, 1024), "Everyday  •  4 steps  •  Saved Offline", fill=EMERALD_PRI, font=load_font(22, bold=True))
    d4.text((172, 1066), "Create a calm, repeatable checklist for future trips.", fill=(73, 99, 93), font=load_font(22))
    d4.rounded_rectangle([140, 1148, 940, 1296], radius=24, fill=(227, 242, 234))
    d4.text((172, 1184), "Theme:   [ System Default ]   [ Emerald Dark ]   [✓ Light ]", fill=CHARCOAL_INK, font=load_font(24, bold=True))
    d4.text((172, 1236), "Haptic Feedback: ON   •   UMP Privacy Controls Ready", fill=EMERALD_PRI, font=load_font(22, bold=True))

    # Developer & Offline Assurance Footer Card
    d4.rounded_rectangle([108, 1380, 972, 1780], radius=32, fill=SURFACE, outline=BORDER_COLOR, width=2)
    d4.text((144, 1420), "100% NATIVE KOTLIN  •  ZERO CLOUD LOCK-IN", fill=MINT_ACCENT, font=load_font(22, bold=True))
    d4.text((144, 1474), "• Works 100% offline with Room SQLite Database\n• Instant search & category filtering\n• Swipe-to-unsave bookmarks with Undo\n• GDPR / CCPA Google UMP Privacy Choices\n• Direct YouTube, Telegram, TikTok & Email links", fill=TEXT_LIGHT, font=load_font(25))
    img4.save(OUT_DIR / "phone_screenshot_4_offline_provault.png", format="PNG")


def generate_tablet_screenshots():
    # Tablet 1 (7-inch & 10-inch landscape 1920 x 1200): Responsive 2-Column Explore Grid
    t1 = make_vertical_gradient(1920, 1200, (26, 56, 48), DARK_BG, mode="RGB")
    dt1 = ImageDraw.Draw(t1)

    draw_flowpebble_emblem(dt1, 130, 110, 110)
    dt1.text((210, 68), "FlowPebble — Tablet & Landscape Multi-Column Experience", fill=TEXT_LIGHT, font=load_font(42, bold=True))
    dt1.text((210, 126), "Responsive sw600dp GridLayoutManager • Instant Offline Search • Material Design 3", fill=MINT_ACCENT, font=load_font(24, bold=True))

    # Tablet Bezel
    dt1.rounded_rectangle([70, 190, 1850, 1130], radius=44, fill=DARK_BG, outline=MINT_ACCENT, width=4)
    dt1.rounded_rectangle([120, 240, 1800, 316], radius=22, fill=SURFACE, outline=BORDER_COLOR, width=2)
    dt1.text((156, 263), "🔍  Search practical offline micro-guides…", fill=SAGE_MUTED, font=load_font(24))

    # 2x3 Grid of cards
    grid_items = [
        ("Review your phone storage safely", "Phone & tech • 4 steps", "Find large files before deleting anything important."),
        ("Set up a 15-minute focus session", "Study & work • 4 steps • 15m Timer", "Turn one vague task into a clear 15-minute sprint."),
        ("Build a reusable packing list", "Everyday • 4 steps", "Create a calm, repeatable checklist for future trips."),
        ("Organize a crowded Downloads folder", "Phone & tech • 4 steps", "Sort first and delete only files you can identify."),
        ("Break a big task into three actions", "Study & work • 4 steps", "Make the next move obvious instead of planning forever."),
        ("Reset a cluttered desk in five minutes", "Everyday • 4 steps • 5m Timer", "A rapid 5-minute physical reset for a calmer workspace."),
    ]
    for idx, (title, meta, desc) in enumerate(grid_items):
        col = idx % 2
        row = idx // 2
        x0 = 120 + col * 856
        y0 = 350 + row * 236
        dt1.rounded_rectangle([x0, y0, x0 + 824, y0 + 212], radius=28, fill=SURFACE, outline=BORDER_COLOR, width=2)
        dt1.text((x0 + 36, y0 + 32), title, fill=TEXT_LIGHT, font=load_font(28, bold=True))
        dt1.text((x0 + 36, y0 + 80), meta, fill=MINT_ACCENT, font=load_font(21, bold=True))
        dt1.text((x0 + 36, y0 + 126), desc, fill=SAGE_MUTED, font=load_font(22))

    t1.save(OUT_DIR / "tablet_7inch_landscape_1920x1200.png", format="PNG")

    # Tablet 2 (10-inch landscape 1920 x 1200): Split Master/Detail Checklist + Focus Timer + Pro Vault
    t2 = make_vertical_gradient(1920, 1200, (24, 52, 44), DARK_BG, mode="RGB")
    dt2 = ImageDraw.Draw(t2)

    draw_flowpebble_emblem(dt2, 130, 110, 110)
    dt2.text((210, 68), "Interactive Step Checklists, Built-In Focus Timer & Pro Vault", fill=TEXT_LIGHT, font=load_font(42, bold=True))
    dt2.text((210, 126), "100% Offline Room SQLite Persistence • Tactile Haptics • One-Tap Native Share", fill=MINT_ACCENT, font=load_font(24, bold=True))

    dt2.rounded_rectangle([70, 190, 1850, 1130], radius=44, fill=DARK_BG, outline=MINT_ACCENT, width=4)

    # Left Pane: Today Hero + Timer
    dt2.rounded_rectangle([116, 236, 860, 640], radius=32, fill=FOREST_START, outline=MINT_ACCENT, width=2)
    dt2.text((156, 274), "ONE SMALL WIN  •  FLOWPEBBLE", fill=MINT_HIGHLIGHT, font=load_font(20, bold=True))
    dt2.text((156, 322), "Useful knowledge\ntravels further.", fill=TEXT_LIGHT, font=load_font(46, bold=True))
    dt2.text((156, 466), "Save a practical guide. Make your own.\nShare it when it could help someone.", fill=TEXT_LIGHT, font=load_font(24))
    dt2.rounded_rectangle([156, 548, 490, 604], radius=26, fill=DARK_BG)
    dt2.text((184, 562), "✓  9 guides completed", fill=MINT_ACCENT, font=load_font(23, bold=True))

    dt2.rounded_rectangle([116, 672, 860, 1080], radius=32, fill=SURFACE, outline=MINT_ACCENT, width=2)
    dt2.text((156, 712), "BUILT-IN FOCUS TIMER  •  DEEP WORK", fill=MINT_HIGHLIGHT, font=load_font(22, bold=True))
    dt2.text((156, 770), "24:59", fill=TEXT_LIGHT, font=load_font(84, bold=True))
    dt2.text((156, 892), "Deep Work Digital Detox Protocol (Pro Vault)", fill=MINT_ACCENT, font=load_font(24, bold=True))
    dt2.rounded_rectangle([156, 956, 420, 1032], radius=24, fill=MINT_ACCENT)
    dt2.text((220, 978), "Start Timer", fill=DARK_BG, font=load_font(26, bold=True))

    # Right Pane: Step-by-Step Interactive Checklist
    dt2.rounded_rectangle([900, 236, 1804, 1080], radius=32, fill=SURFACE, outline=BORDER_COLOR, width=2)
    dt2.text((944, 276), "Deep Work Digital Detox Protocol", fill=TEXT_LIGHT, font=load_font(34, bold=True))
    dt2.text((944, 326), "Study & work  •  4 steps  •  3 of 4 completed", fill=MINT_ACCENT, font=load_font(22, bold=True))

    t_steps = [
        ("1", "Enable Do Not Disturb and place phone out of reach.", True),
        ("2", "Close all messaging tabs, email clients, and feeds.", True),
        ("3", "Open only the single document or IDE required.", True),
        ("4", "Commit to a 25-minute block before checking inboxes.", False),
    ]
    ty = 392
    for num, stext, done in t_steps:
        dt2.rounded_rectangle([944, ty, 1760, ty + 124], radius=22, fill=DARK_BG, outline=MINT_ACCENT if done else BORDER_COLOR, width=2)
        dt2.text((976, ty + 44), f"{num}.  {stext}", fill=TEXT_LIGHT, font=load_font(23, bold=True))
        ty += 144

    dt2.rounded_rectangle([944, 982, 1760, 1052], radius=24, fill=MINT_ACCENT)
    dt2.text((1210, 1002), "✓  Mark Guide as Completed", fill=DARK_BG, font=load_font(26, bold=True))

    t2.save(OUT_DIR / "tablet_10inch_landscape_1920x1200.png", format="PNG")


if __name__ == "__main__":
    generate_app_icon()
    generate_feature_graphic()
    generate_phone_screenshots()
    generate_tablet_screenshots()
    print(f"All 8 Google Play Console PNG graphics generated in: {OUT_DIR}")
