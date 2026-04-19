# CineBand — testing account and QA checklist

## Primary QA user (seeded by the API)

On startup, the Spring Boot app creates this account **only if** the email and nick are not already taken.

| Field | Value |
|--------|--------|
| **Email** | `cineband.tester@example.com` |
| **Password** | `CineBandTest!2026` |
| **Nick (handle)** | `cineband_tester` |
| **Display name** | `CineBand Tester` |

Override via `application.properties` (or env):

- `cineband.testing-user.email`
- `cineband.testing-user.password`
- `cineband.testing-user.nick`
- `cineband.testing-user.display-name`
- `cineband.testing-user.enabled` — set to `false` in production if you do not want auto-seeding.

## Legacy demo users (if present in DB)

Users with ids `1`–`5` may get `nick` `demo1` … `demo5` and password `demo123` from `DemoAccountsInitializer` when their password hash was empty. Your dataset may differ.

---

## How to verify that models “learn”

1. **Run services**: MySQL with `system_rekomendacji`, **recommendation-ml** on port **8000**, **recommendation-api** on **8080**, **recommendation-web** (or proxy to API).
2. **Sign in** as the testing user above.
3. **Home → For you**: click **Refresh picks** — hybrid/KNN/MF use your user id; a brand-new account may get sparse or generic results until ratings exist.
4. **Surprise duel**: **Get two picks** loads **two** films. Use **Like / Dislike** on the home cards (when signed in), or open a film and rate on the detail page (`?ref=surprise`). That writes learning signals the ML pipeline can read on the next request.
5. **Profile**: check **Pick history** and **Not for me** (dislikes) to confirm persistence.
6. **Repeat** “For you” after several reactions — scores should shift as the matrix gains rows for this user (exact behavior depends on catalog size and algorithm).

If recommendations never change, check: ML service logs, DB connectivity from Python, and that `Oceny` / feedback rows exist for `cineband_tester`’s user id.

---

## Product note (honest, non-technical)

**What this project is**

CineBand is a **credible full-stack + ML demo**: auth, social-style profile, feedback into a recommender, and a clear UX path (browse, surprise duel, personalized list). That is **more than a toy** if the stack runs end-to-end and the data loop is real.

**Commercial outlook**

Consumer movie discovery is **crowded** (streaming apps, Letterboxd, JustWatch, etc.). This codebase does **not** by itself guarantee revenue. Turning it into a business would need a **sharp niche** (e.g. festival, regional cinema, education, B2B analytics), **distribution**, **rights-safe assets**, and **trust** — not only algorithms.

**Verdict**

- **Portfolio / technical credibility**: strong if you can demo it reliably.
- **“Fun project only”**: unfair if learning + API + ML are wired; it is an **early product skeleton**, not a finished consumer app.
- **“0 chance to get money”**: too strong; the chance is **not zero**, but **uncertain** and **not automatic** — it depends on positioning and execution outside this repo.

Use this document for internal QA; rotate passwords before any public deployment.
