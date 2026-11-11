# FlowPebble — 100% Smartphone-to-Google-Play-Store Production Manual

**Application Name:** FlowPebble  
**Tagline:** Small Steps, Shared — Practical Offline Micro-Guides  
**Package Name (`namespace` & `applicationId`):** `com.flowpebble.microguides`  
**Target / Compile SDK:** Android 15 (API 35) | **Min SDK:** Android 8.0 (API 26)  
**AdMob Production App ID:** `ca-app-pub-8550257135927026~2543487910`  
**AdMob Publisher ID:** `pub-8550257135927026`  
**GitHub Repository:** `https://github.com/FlowPebble/flowpebble-app`  
**GitHub Pages Base URL:** `https://flowpebble.github.io/flowpebble-app/`

This production runbook is engineered specifically for a mobile-only workflow. Every step below—from repository creation, cloud keystore generation, GitHub Secrets setup, GitHub Pages legal hosting, to downloading your signed `.aab` and `.apk` and submitting to Google Play Console—can be completed 100% inside your smartphone browser (Chrome/Safari/Firefox Mobile) without a desktop computer, local JDK, or Android Studio.

## Phase 1: Creating `FlowPebble/flowpebble-app` & Uploading the Project from Your Phone

1. **Create the Repository on Mobile Browser:**
   - Open `https://github.com/new` in your smartphone browser and sign in to the **FlowPebble** account.
   - Set **Repository name** to `flowpebble-app`.
   - Set visibility to **Public** (required for free GitHub Pages hosting of `privacy-policy.html`, `terms-of-service.html`, and `app-ads.txt`).
   - Check **Add a README file** so the `main` branch is initialized immediately, then tap **Create repository**.

2. **Upload the Complete Project Tree from Your Smartphone:**
   - Download `flowpebble-app-complete.zip` onto your phone and tap it in your phone's built-in **Files** app to extract the `flowpebble-app` folder.
   - **Fastest Mobile Browser Method (GitHub Web Editor / Codespaces in Mobile Browser):**
     - Navigate to `https://github.com/FlowPebble/flowpebble-app`.
     - Tap the **Code** button (green button) → switch to the **Codespaces** tab → tap **Create codespace on main** (or open `https://github.dev/FlowPebble/flowpebble-app` in Desktop Site mode on your mobile browser).
     - In the cloud terminal or Explorer pane, upload `flowpebble-app-complete.zip` directly into the workspace root and run:
       ```bash
       unzip -o flowpebble-app-complete.zip
       cp -r flowpebble-app/* flowpebble-app/.github flowpebble-app/.gitignore .
       rm -rf flowpebble-app flowpebble-app-complete.zip
       git add -A
       git commit -m "Initial commit: 100% Native Kotlin FlowPebble production release"
       git push origin main
       ```
   - **Alternative Pure GitHub Web Upload (No Terminal):**
     - Open `https://github.com/FlowPebble/flowpebble-app` in your mobile browser with **Desktop site** enabled in browser settings.
     - Tap **Add file** → **Upload files**, select the extracted contents of `flowpebble-app`, and tap **Commit changes** directly to `main`.

## Phase 2: Generating Your Upload Keystore 100% on Your Phone & Configuring All 7 GitHub Secrets

Because you have zero access to a desktop `keytool` CLI, `.github/workflows/android-release.yml` includes a built-in **One-Click Cloud Keystore Generator Job** (`generate-keystore`).

### Step 2.1 — Pre-Configure Your Keystore Passwords & AdMob Unit IDs in GitHub Secrets
1. In your mobile browser, open `https://github.com/FlowPebble/flowpebble-app/settings/secrets/actions`.
2. Tap **New repository secret** and create the following **6 secrets** first:

| Secret Name | Value to Enter |
| :--- | :--- |
| `KEY_ALIAS` | `flowpebble-key` |
| `KEY_PASSWORD` | A strong password of your choice (at least 12 characters, no spaces) |
| `STORE_PASSWORD` | The exact same strong password as `KEY_PASSWORD` |
| `ADMOB_BANNER_ID` | Your production AdMob Anchored Adaptive Banner Unit ID (`ca-app-pub-8550257135927026/XXXXXXXXXX`) |
| `ADMOB_INTERSTITIAL_ID` | Your production AdMob Interstitial Unit ID (`ca-app-pub-8550257135927026/YYYYYYYYYY`) |
| `ADMOB_REWARDED_ID` | Your production AdMob Rewarded Ad Unit ID (`ca-app-pub-8550257135927026/ZZZZZZZZZZ`) |

### Step 2.2 — Trigger the One-Click Cloud Keystore Generator from Your Phone
1. Go to `https://github.com/FlowPebble/flowpebble-app/actions`.
2. Tap **FlowPebble Android Release & Play Store CI/CD**.
3. Tap the **Run workflow** dropdown button.
4. Check the box for **`generate_new_keystore`** (`true`) and tap the green **Run workflow** button.
5. Wait ~60 seconds for the `generate-keystore` job to finish, then scroll to the bottom of the workflow summary page under **Artifacts** and download **`flowpebble-generated-keystore-DO-NOT-SHARE`**.
6. Extract that zip on your phone:
   - Save `flowpebble-upload.jks` in a safe, backed-up private folder (e.g., encrypted cloud drive).
   - Open `KEYSTORE_BASE64.txt`, select all text, and copy the single-line Base64 string.
7. Return to `https://github.com/FlowPebble/flowpebble-app/settings/secrets/actions`, tap **New repository secret**, set Name to **`KEYSTORE_BASE64`**, paste the Base64 string as the Value, and tap **Add secret**.
8. Delete the temporary `flowpebble-generated-keystore-DO-NOT-SHARE` artifact from the GitHub Actions run page so your upload keystore is stored only in your private backup and in `secrets.KEYSTORE_BASE64`.

## Phase 3: Enabling GitHub Pages for Legal Policies & `app-ads.txt`

1. In your mobile browser, navigate to `https://github.com/FlowPebble/flowpebble-app/settings/pages`.
2. Under **Build and deployment** → **Source**, select **GitHub Actions** (our `deploy-legal-pages` job in `android-release.yml` automatically publishes the `/docs` directory on every release build!).
   - *Alternative branch mode:* You can also select **Deploy from a branch** → Branch: `main` → Folder: `/docs` → tap **Save**.
3. Within 60 seconds, verify that these three URLs load live in your mobile browser:
   - **Privacy Policy:** `https://flowpebble.github.io/flowpebble-app/privacy-policy.html`
   - **Terms of Service:** `https://flowpebble.github.io/flowpebble-app/terms-of-service.html`
   - **AdMob `app-ads.txt`:** `https://flowpebble.github.io/flowpebble-app/app-ads.txt` (displays `google.com, pub-8550257135927026, DIRECT, f08c47fec0942fa0`)

## Phase 4: Running the Release Build & Downloading Signed `.aab`, `.apk`, and `mapping.txt`

1. Open `https://github.com/FlowPebble/flowpebble-app/actions`.
2. Tap **FlowPebble Android Release & Play Store CI/CD** → **Run workflow** (leave `generate_new_keystore` unchecked `false`) → tap **Run workflow**.
3. GitHub Actions will:
   - Set up JDK 17 (Temurin) and Android SDK 35.
   - Decode `secrets.KEYSTORE_BASE64` into `app/release-keystore.jks`.
   - Inject your 3 production AdMob Unit IDs (`ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_REWARDED_ID`) into `BuildConfig`.
   - Run `./gradlew lintRelease testReleaseUnitTest bundleRelease assembleRelease`.
   - Wipe `app/release-keystore.jks` with `if: always()`.
4. When the run turns green, scroll down to **Artifacts** on your phone and tap **`flowpebble-play-store-release-bundle`** to download the ZIP containing:
   - `app-release.aab` (Signed Android App Bundle for Google Play Console)
   - `app-release.apk` (Signed Release APK for direct testing on your phone)
   - `mapping.txt` (R8/ProGuard deobfuscation file to upload alongside `.aab` in Play Console)
   - All 8 rendered Play Store PNG graphics (`512x512` icon, `1024x500` feature graphic, 4 phone screenshots, 2 tablet screenshots)

## Phase 5: Exact Click-by-Click Google Play Console & AdMob Declarations

### 5.1 — Creating the App in Google Play Console
1. Sign in to `https://play.google.com/console` on your phone browser.
2. Tap **Create app**:
   - **App name:** `FlowPebble`
   - **Default language:** `English (United States) – en-US`
   - **App or game:** `App`
   - **Free or paid:** `Free`
   - Check the Developer Program Policies and US export laws boxes → tap **Create app**.

### 5.2 — App Content Declarations (Exact Answers)

| Play Console Section | Exact Selection / Answer to Enter |
| :--- | :--- |
| **Privacy policy** | Enter `https://flowpebble.github.io/flowpebble-app/privacy-policy.html` → Save. |
| **App access** | Select **"All functionality in my app is available without any access restrictions"** (FlowPebble is 100% offline-first and requires no login). |
| **Ads** | Select **"Yes, my app contains ads"** → Save. |
| **Content rating (IARC)** | Enter your contact email (`digitalizationethiopian@gmail.com`) → Category: **Utility, Productivity, Communication, or Other** → Answer **"No"** to Violence, Sexual Content, Profanity, Controlled Substances, User-Generated Online Content Sharing (sharing is local device share-sheet only), and Location Sharing → Submit & Apply rating. |
| **Target audience and content** | Select **13–15**, **16–17**, and **18 and over** (Do **NOT** select under 13, so you avoid Google Play Families policy traps) → Appeal to children: **"No"** → Save. |
| **News apps** | Select **"No, my app is not a news app"** → Save. |
| **COVID-19 contact tracing / status** | Select **"My app is not a publicly available COVID-19 contact tracing or status app"** → Save. |
| **Government apps** | Select **"No"** → Save. |
| **Financial features** | Select **"My app doesn't provide any financial features"** → Save. |
| **Health apps** | Select **"My app does not have any health features"** (productivity/focus timers only) → Save. |
| **Advertising ID** | Does your app use Advertising ID? Select **"Yes"** → Reason: check **Advertising or marketing** and **Analytics** → Save. |

### 5.3 — Data Safety Form (Exact Checkboxes for AdMob + UMP + Offline Room DB)
Because FlowPebble stores all user guides offline on the device and only the Google Mobile Ads SDK (AdMob) transmits network signals:
1. **Data collection and security:**
   - Does your app collect or share any of the required user data types? → **Yes**
   - Is all of the user data collected by your app encrypted in transit? → **Yes** (`usesCleartextTraffic="false"` enforces TLS/HTTPS).
   - Do you provide a way for users to request that their data is deleted? → Select **"No, my app does not allow users to create an account"** (and note that Advertising ID can be reset/deleted in device settings).
2. **Data types to check:**
   - **Location** → Check **Approximate location** (inferred from IP address by AdMob).
   - **App activity** → Check **App interactions** (ad views/clicks measured by AdMob).
   - **App info and performance** → Check **Crash logs** and **Diagnostics** (AdMob SDK diagnostics).
   - **Device or other IDs** → Check **Device or other IDs** (Google Advertising ID).
3. **For each of those 4 checked data types, answer the details screen identically:**
   - Is this data collected, shared, or both? → Check **Collected** and **Shared** (with third-party ad partners via AdMob).
   - Is this data processed ephemerally? → **No**.
   - Is this data required for your app, or can users choose whether it's collected? → **Users can choose whether this data is collected** (via Google UMP GDPR/CCPA consent dialog and Android Ad ID controls).
   - Why is this user data collected/shared? → Check **Advertising or marketing**, **Analytics**, and **Fraud prevention, security, and compliance**.

### 5.4 — Store Listing Metadata & Graphics Upload
1. Go to **Grow** → **Store presence** → **Main store listing**:
   - **App name:** `FlowPebble`
   - **Short description (under 80 chars):** `Small Steps, Shared — Create, complete & share offline 3–5 step micro-guides.`
   - **Full description:**
     ```text
     FlowPebble helps you turn everyday tasks, digital organization, and deep focus routines into calm, actionable 3-to-5 step micro-guides ("Small Steps, Shared").

     Designed to work 100% offline with a fast native Android Room SQLite database, FlowPebble gives you practical checklists right when you need them—without requiring an account, cloud login, or constant internet connection.

     KEY FEATURES:
     • Starter Micro-Guides Ready Offline: Immediately access practical 4-step checklists across Everyday, Phone & tech, Study & work, Travel, Create & share, and Wellness & focus.
     • Interactive Step Checklists & Progress Tracking: Check off each step with crisp haptic feedback and watch your real-time progress bar advance.
     • Built-In Focus & Reset Timers: Launch the integrated 15-minute focus sprint timer or 5-minute desk reset timer directly inside timed guides.
     • Create Your Own 3–5 Step Micro-Guides: Capture your own repeatable workflows with custom titles, summaries, category tags, and icon badges.
     • Made to Be Shared: Export any micro-guide as a clean, numbered text checklist via Android's native Share Sheet, or send text from your notes/browser directly into FlowPebble.
     • Pro Guide Vault: Optionally unlock bonus deep-focus guides and quick-fill templates.
     • Material Design 3 Dark & Light Themes: Switch seamlessly between Signature Emerald/Pebble Dark Mode, Crisp Alabaster Light Mode, or System Default.
     ```
2. Upload the 8 PNG files from `flowpebble-play-store-graphics.zip`:
   - **App icon:** `app_icon_512x512.png`
   - **Feature graphic:** `feature_graphic_1024x500.png`
   - **Phone screenshots:** Upload `phone_screenshot_1_today_dashboard.png` through `phone_screenshot_4_offline_provault.png`
   - **7-inch & 10-inch tablet screenshots:** Upload `tablet_7inch_landscape_1920x1200.png` and `tablet_10inch_landscape_1920x1200.png`
3. In **Store settings**, set **Website** to `https://flowpebble.github.io/flowpebble-app/` (this allows AdMob to verify `https://flowpebble.github.io/flowpebble-app/app-ads.txt` automatically) and set **Email address** to `digitalizationethiopian@gmail.com`.

### 5.5 — Configuring AdMob GDPR & US State Regulations Messages
1. Sign in to `https://apps.admob.com` on your phone browser and select **FlowPebble** (`ca-app-pub-8550257135927026~2543487910`).
2. Tap **Privacy & messaging** in the sidebar:
   - **European regulations (GDPR):** Tap **Create message** → Select app `FlowPebble` → Enter Privacy Policy URL `https://flowpebble.github.io/flowpebble-app/privacy-policy.html` → Enable "Consent" and "Manage options" → Publish.
   - **US state regulations (CCPA/CPRA):** Tap **Create message** → Select app `FlowPebble` → Publish.
   - Because `ConsentManager.kt` uses Google's UMP SDK (`com.google.android.ump:user-messaging-platform:3.1.0`), your app will automatically display these forms on launch where required and expose the **Advertising Privacy Choices** button inside Settings.

### 5.6 — Internal Testing, 12-Testers / 14-Days Closed Testing & Production Rollout
1. **Internal Testing (Immediate Verification):**
   - Go to **Testing** → **Internal testing** → **Create new release**.
   - Tap **Choose signing key** → **Use Google-generated key**.
   - Upload `app-release.aab` and `mapping.txt` from your GitHub Actions artifact zip.
   - Save and roll out to Internal testing, then install on your phone via the tester opt-in link.
2. **Closed Testing (For Personal Developer Accounts Created After Nov 13, 2023):**
   - Promote your release to **Closed testing – Alpha**.
   - Add at least **12 testers** by email (friends, colleagues, or testing community) and share the Web Opt-In Link.
   - Once 12 testers remain opted-in continuously for **14 days**, tap **Apply for production** on your Dashboard.
3. **Production Release:**
   - Go to **Production** → **Create new release**, select `app-release.aab` from your artifact library, and tap **Start rollout to Production**.
