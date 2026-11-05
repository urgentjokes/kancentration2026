# Agent Instructions & Guidelines

## Git Source Control Rule
- **Do NOT commit or push to git unless explicitly instructed by the user.**
- Performing local development, refactoring, building, unit testing, and deploying to connected devices or emulators should NOT trigger git commits or git pushes automatically.
- Only run `git commit` or `git push` when the user explicitly requests a commit or push.

## Confusable Kanji Pairing Guidelines
- **Focus strictly on visual similarities and subtle differences**: Look for near-identical stroke arrangements, subtle radical variations (e.g., 持 vs 特), minute aspect-ratio/proportional differences (e.g., 日 vs 曰), accent strokes/dots (e.g., 木 vs 本, 王 vs 玉, 大 vs 犬), subtle component replacements (e.g., 林 vs 村, 字 vs 学), or cross-script lookalikes like Kanji vs Katakana traps (e.g., 八 vs ハ, 夕 vs タ).
- **Avoid non-visual pairings**: Do not pair characters based on conceptual/semantic opposites or relationships (e.g., avoid 上 vs 下, 林 vs 森), simple compositional enclosure without genuine visual confusion (e.g., avoid 夕 vs 名, 中 vs 虫), or differing internal complexity without visual mimicry (e.g., avoid 田 vs 四, 十 vs 千).

## Connected Hardware & Devices
- **User's Phone (`SM-S948U`)**: Samsung Galaxy S26 Ultra (not S24).

## Build & Device Deployment
- **Automatic Deployment to Phone**: After completing code changes requested by the user, build and push/install the app to the user's phone (`SM-S948U` / Samsung Galaxy S26 Ultra), launching the updated build. (Note: this is deploying via `adb install` to the physical phone, NOT a `git commit` or `git push` to version control).
