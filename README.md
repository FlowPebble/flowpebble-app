# FlowPebble — Small Steps, Shared (`com.flowpebble.microguides`)

A complete, production-grade, 100% Native Android Kotlin offline-first productivity & micro-learning application engineered for Google Play Console and Google AdMob + UMP compliance, built and signed 100% in the cloud via GitHub Actions.

## Architecture Highlights
- **100% Native Android Kotlin**: MVVM, Kotlin Coroutines & `StateFlow`, Room SQLite Database (`@Entity`, `@Dao`, `@Database` with automatic first-install seeding), Jetpack Preferences DataStore, ViewBinding, Navigation Component, and Android 12+ `SplashScreen` API.
- **Material Design 3 (M3)**: Signature Emerald/Pebble Dark Palette (`#101B1A` / `#182624` / `#79E0B2`) and Alabaster Mint Light Mode (`#F4FBF7` / `#14533C`) with instant runtime theme switching.
- **AdMob & UMP Privacy Compliance**: Google UMP consent gathering gates all AdMob initialization; Anchored Adaptive Banner in a dedicated non-overlapping bottom container; lifecycle-aware Interstitials with strict frequency capping (every 4 completed guides + 180s cooldown); and voluntary Rewarded Ad unlocking the Pro Guide Vault.
- **100% Smartphone Cloud CI/CD**: `.github/workflows/android-release.yml` compiles, lints, tests, renders Google Play Store graphics, signs both `.aab` and `.apk`, exports R8 `mapping.txt`, and deploys `/docs` (`privacy-policy.html`, `terms-of-service.html`, `app-ads.txt`) to GitHub Pages.
