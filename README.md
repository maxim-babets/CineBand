# CineBand

**Repository:** [github.com/maxim-babets/CineBand](https://github.com/maxim-babets/CineBand)

Movie discovery with a **Spring Boot** API, **FastAPI** recommender (KNN / matrix factorization / hybrid), **MySQL**, and a **React + TypeScript** UI. Users get a feed, profiles (followers / following), “surprise duel” random pairs, and personalized **For you** lists when signed in. Likes and dislikes from the right contexts feed **learning** (ratings / signals) for the next ML run.

---

## Quick start — run the application locally

Run these **in order** (four moving parts: MySQL, then ML, then API, then web UI).

1. **MySQL** — Start MySQL and ensure database `system_rekomendacji` exists with your schema and data (see [Database setup](#database-setup)). Default connection in code: `localhost:3306`, user `root`, empty password — adjust in `recommendation-api/src/main/resources/application.properties` if needed.

2. **ML service (port 8000)** — From `recommendation-ml`:

   ```bash
   cd recommendation-ml
   python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
   pip install -r requirements.txt
   uvicorn app.main:app --host 127.0.0.1 --port 8000
   ```

3. **Spring Boot API (port 8080)** — From `recommendation-api`:

   ```bash
   cd recommendation-api
   mvn spring-boot:run
   ```

   Check: [http://127.0.0.1:8080/](http://127.0.0.1:8080/) returns JSON. The API expects the ML service at `http://127.0.0.1:8000` (`ml.service.base-url`).

4. **Web UI (Vite)** — From `recommendation-web` (proxies `/api` to the API on 8080):

   ```bash
   cd recommendation-web
   npm install
   npm run dev
   ```

   Open the URL Vite prints (usually **http://localhost:5173**). Use the browser URL the terminal shows — `localhost` vs `127.0.0.1` both work with the default proxy.

**Test login (if seeding is enabled):** see [`docs/TESTING_ACCOUNT.md`](docs/TESTING_ACCOUNT.md) — default test credentials are listed in `application.properties` (`cineband.testing-user.*`).

For troubleshooting, TMDB notes, and the full API list, see the sections below.

---

## Repository layout

| Path | Role |
|------|------|
| `recommendation-api/` | Spring Boot 3, JWT auth, REST, calls ML over HTTP. Core public routes live in `PublicEndpointsController` (same package as the main class) so `/api/login` and `/api/surprise-pair` always register. |
| `recommendation-ml/` | FastAPI service: `/recommend`, reads ratings from MySQL |
| `recommendation-web/` | Vite + React UI; in dev, `/api` is proxied to the API |
| `Rekomendacjafilmow/` | SQL migrations and legacy scripts; base data may include `Movie.txt` |
| `docs/` | Extra notes (e.g. testing account) |

---

## Prerequisites

- **JDK 17+**, **Maven 3.9+**
- **Python 3.10+** (3.9 may work; align `numpy` with your Python)
- **Node.js 20+** (for the web app)
- **MySQL 8+** with a database (default name: `system_rekomendacji`)

---

## Database setup

1. Create the database and load your **Filmy** / **Oceny** / user schema (e.g. from your course materials or `Movie.txt` if applicable).
2. Apply migrations **in a sensible order** (adjust if a step already exists):

   - `CineBand_auth_social.sql` — auth columns, wall, picks  
   - `CineBand_banners_feedback.sql` — `banner_url`, feedback table  
   - `Movie_watch_providers.sql` — “where to watch” (if you use it)  
   - `CineBand_social_follow.sql` — follow graph  

3. You need **at least two rows** in `Filmy` for the surprise duel endpoint to succeed.

Connection defaults match `recommendation-api` (`application.properties`) and `recommendation-ml` (env / defaults): typically `localhost`, user `root`, empty password — **change for production**.

---

## Configuration (secrets)

| Concern | Where |
|--------|--------|
| MySQL URL / user / password | `recommendation-api/src/main/resources/application.properties` |
| JWT signing secret | `cineband.jwt.secret` — **must be long and random in production** |
| ML service URL | `ml.service.base-url` (default `http://127.0.0.1:8000`) |
| Python DB access | `recommendation-ml/.env` (see `.env.example`) |

**Do not commit** real secrets or API keys. Use environment variables or a private config in deployment.

---

## Run locally (detailed — three processes + DB)

MySQL must be running first. Then start ML, API, and web as below.

### 1. ML service (port 8000)

```bash
cd recommendation-ml
python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
# Optional: copy .env.example to .env and set DB_* if needed
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

### 2. API (port 8080)

```bash
cd recommendation-api
mvn spring-boot:run
# or: mvn -q -DskipTests package && java -jar target/recommendation-api-*.jar
```

Opening **http://127.0.0.1:8080/** should return a small **JSON** body (a servlet filter handles `/` before MVC). The React UI is separate — use Vite below. If you still see an old HTML error page, stop every Java process on 8080 and run **`mvn clean package`** again.

### 3. Web (Vite dev — proxies `/api` → 8080)

```bash
cd recommendation-web
npm install
npm run dev
```

Open the URL Vite prints (usually `http://localhost:5173`).  
`npm run preview` (after `npm run build`) also proxies `/api` — keep the API on **8080**.

---

## Testing account

A QA user can be auto-created on API startup; credentials and checks are in **`docs/TESTING_ACCOUNT.md`**. Disable seeding in production with `cineband.testing-user.enabled=false`.

---

## TMDB images and banners

The app does **not** call TMDB at runtime. It only displays whatever URL you store in **`Filmy.banner_url`** (see `CineBand_banners_feedback.sql`). To use **The Movie Database (TMDB)** artwork:

1. **Account**  
   Create an account at [themoviedb.org](https://www.themoviedb.org/), open **Settings → API**, and request an API key (read the current [API terms](https://www.themoviedb.org/documentation/api/terms-of-use) — attribution and usage rules apply).

2. **Find the movie and image path**  
   Use the [search endpoint](https://developer.themoviedb.org/reference/search-movie), e.g.:

   ```http
   GET https://api.themoviedb.org/3/search/movie?query=Inception&api_key=YOUR_KEY
   ```

   From the JSON, note `results[].id` (TMDB id) and `backdrop_path` or `poster_path`.

3. **Build a stable image URL**  
   TMDB hosts files under a configurable base URL. Fetch configuration once:

   ```http
   GET https://api.themoviedb.org/3/configuration?api_key=YOUR_KEY
   ```

   Combine `images.secure_base_url` (e.g. `https://image.tmdb.org/t/p/`) with a size (e.g. `w780`) and the path, for example:

   `https://image.tmdb.org/t/p/w780/abc123....jpg`

4. **Write into MySQL**  

   ```sql
   UPDATE Filmy
   SET banner_url = 'https://image.tmdb.org/t/p/w780/<path-from-api>'
   WHERE id_filmu = <your_row_id>;
   ```

   Match films by title/year manually, or add a **`tmdb_id`** column later and sync with a small script (not included here).

5. **GitHub / public repos**  
   - Put **`TMDB_API_KEY` only in `.env` or CI secrets**, never in the repo.  
   - Prefer storing **final HTTPS image URLs** in `banner_url` so the running app does not need your TMDB key at all.

Placeholder banners in the sample SQL use `picsum.photos`; replace those URLs when you move to TMDB.

---

## API surface (short)

- **Public:** `GET /api/movies` (browse), `GET /api/surprise-pair` (two random films for Surprise duel), `GET /api/movies/{id}`, `GET /api/movies/random`, `GET /api/posts`, `GET /api/users/...`  
- **Auth:** `POST /api/register`, `POST /api/login` (aliases: `POST /api/auth/register`, `POST /api/auth/login`)  
- **Authenticated:** `GET /api/me/...` (recommendations, picks, feedback, profile card, follow)

---

## Troubleshooting

| Symptom | Things to check |
|--------|------------------|
| `502` from API on recommendations | ML service not running or wrong `ml.service.base-url` |
| Surprise duel **HTTP 404** | The UI calls **`GET /api/surprise-pair`**. Restart the API after `mvn clean package` and try: `curl -i http://127.0.0.1:8080/api/surprise-pair` — expect **200** or **422** (not enough films), not 404. |
| Surprise duel **HTTP 422** | Fewer than two films in `Filmy` |
| Frontend cannot reach API | Use `npm run dev` / `npm run preview` so `/api` proxies to port 8080, or put the UI behind the same reverse proxy as the API |
| Empty “For you” | New user with almost no ratings; add feedback / picks and retry |

---

## Publishing to GitHub

Official repo: **[github.com/maxim-babets/CineBand](https://github.com/maxim-babets/CineBand)**.

1. **Clean build artifacts** (do not commit): `recommendation-api/target/`, `recommendation-web/node_modules/`, `recommendation-web/dist/`, `recommendation-ml/.venv/` — all are in `.gitignore`.
2. **Secrets**: Use placeholder JWT / DB settings in `application.properties` for a public demo, or override with env vars in deployment. Rotate anything you’ve used on a real server before pushing.
3. **Clone or push** — If you already have the project locally:

```bash
cd /path/to/CineBand
git remote add origin https://github.com/maxim-babets/CineBand.git   # skip if origin exists
git branch -M main
git push -u origin main
```

   For a **new** clone from GitHub:

```bash
git clone https://github.com/maxim-babets/CineBand.git
cd CineBand
```

4. **Repo description** (suggestion): *Spring Boot + FastAPI movie recommendations, React UI, MySQL — portfolio demo.*

---

## Contributing and license

Licensed under the **MIT License** — see `LICENSE`. Issues and PRs are welcome. Document schema changes under `Rekomendacjafilmow/`, and run `mvn test` / `pytest` / `npm run build` before merging when relevant.

---

## Code style note (for maintainers)

The codebase mixes hand-written domain logic with a consistent layout (controllers, services, React pages). If you use AI tools, do a **human pass** on public docs, error messages, and anything user-facing so the tone stays yours — reviewers care more about clarity and tests than whether every line was typed by hand.
