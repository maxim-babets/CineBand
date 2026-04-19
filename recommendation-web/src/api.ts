import type {
  AuthResponse,
  DislikedTitle,
  Film,
  Method,
  MovieDetail,
  Pick,
  ProfileCard,
  RecommendResponse,
  WallPost,
} from './types'

function messageFromFailedResponse(body: string, status: number, hint: string): string {
  const trimmed = body.trim()
  if (trimmed.startsWith('<!') || trimmed.startsWith('<html')) {
    return (
      'No API on this origin (got HTML). Run `npm run dev` or `npm run preview` so /api proxies to Spring on port 8080, ' +
      'or host the built files behind the same server as the API.'
    )
  }
  try {
    const j = JSON.parse(trimmed) as { message?: string; error?: string; path?: string; status?: number }
    if (typeof j.message === 'string' && j.message.length > 0) {
      return j.message
    }
    if (status === 404 && j.path) {
      return (
        `API route not found (${j.path}). Restart recommendation-api after \`mvn clean package\`, ` +
        `or check the UI proxies /api to port 8080 (npm run dev).`
      )
    }
  } catch {
    /* not JSON */
  }
  return trimmed || `${hint}: HTTP ${status}`
}

async function handle<T>(res: Response, hint: string): Promise<T> {
  if (res.status === 502) {
    throw new Error(
      'Backend could not reach the ML service. Start recommendation-ml on port 8000, then Spring on 8080.'
    )
  }
  if (res.status === 401) {
    throw new Error('Session expired. Sign in again.')
  }
  if (!res.ok) {
    const text = await res.text()
    throw new Error(messageFromFailedResponse(text, res.status, hint))
  }
  return res.json() as Promise<T>
}

async function handleVoid(res: Response, hint: string): Promise<void> {
  if (res.status === 502) {
    throw new Error(
      'Backend could not reach the ML service. Start recommendation-ml on port 8000, then Spring on 8080.'
    )
  }
  if (res.status === 401) {
    throw new Error('Session expired. Sign in again.')
  }
  if (!res.ok) {
    const text = await res.text()
    throw new Error(messageFromFailedResponse(text, res.status, hint))
  }
}

export async function fetchMovies(): Promise<Film[]> {
  const res = await fetch('/api/movies')
  return handle<Film[]>(res, 'Could not load movies')
}

export async function fetchMovieDetail(id: number): Promise<MovieDetail> {
  const res = await fetch(`/api/movies/${id}`)
  return handle<MovieDetail>(res, 'Could not load movie')
}

export async function fetchRandomMovie(): Promise<MovieDetail> {
  const res = await fetch('/api/movies/random')
  return handle<MovieDetail>(res, 'Could not pick a random movie')
}

/** Two random films for Surprise duel — dedicated path (does not share GET /api/movies). */
export async function fetchSurpriseDuo(): Promise<MovieDetail[]> {
  const res = await fetch('/api/surprise-pair')
  return handle<MovieDetail[]>(res, 'Could not load surprise pair')
}

export type FeedbackSource = 'SURPRISE' | 'ML_FEED'

export async function postRecommendationFeedback(
  token: string,
  movieId: number,
  sentiment: 'LIKE' | 'DISLIKE',
  source: FeedbackSource
): Promise<void> {
  const res = await fetch('/api/me/feedback', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ movieId, sentiment, source }),
  })
  await handle(res, 'Could not save feedback')
}

export async function fetchFeedbackDislikes(token: string): Promise<DislikedTitle[]> {
  const res = await fetch('/api/me/feedback/dislikes', {
    headers: { Authorization: `Bearer ${token}` },
  })
  return handle<DislikedTitle[]>(res, 'Could not load dislikes')
}

export async function fetchMyRecommendations(
  token: string,
  method: Method,
  limit: number,
  kNeighbors: number
): Promise<RecommendResponse> {
  const params = new URLSearchParams({
    method,
    limit: String(limit),
    kNeighbors: String(kNeighbors),
  })
  const res = await fetch(`/api/me/recommendations?${params}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  return handle<RecommendResponse>(res, 'Could not load recommendations')
}

export async function loginRequest(email: string, password: string): Promise<AuthResponse> {
  const res = await fetch('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  return handle<AuthResponse>(res, 'Login failed')
}

export async function registerRequest(
  displayName: string,
  nick: string,
  email: string,
  password: string
): Promise<AuthResponse> {
  const res = await fetch('/api/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ displayName, nick, email, password }),
  })
  return handle<AuthResponse>(res, 'Registration failed')
}

export async function fetchWall(token: string | null): Promise<WallPost[]> {
  const res = await fetch('/api/posts', {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  return handle<WallPost[]>(res, 'Could not load feed')
}

export async function createPost(token: string, content: string): Promise<void> {
  const res = await fetch('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ content }),
  })
  await handle(res, 'Could not post')
}

export async function reactPost(token: string, postId: number, type: 'LIKE' | 'DISLIKE'): Promise<void> {
  const res = await fetch(`/api/posts/${postId}/reactions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ type }),
  })
  await handle(res, 'Could not react')
}

export async function fetchMyPicks(token: string): Promise<Pick[]> {
  const res = await fetch('/api/me/picks', {
    headers: { Authorization: `Bearer ${token}` },
  })
  return handle<Pick[]>(res, 'Could not load picks')
}

export async function fetchPicksByNick(nick: string): Promise<Pick[]> {
  const res = await fetch(`/api/users/by-nick/${encodeURIComponent(nick)}/picks`)
  return handle<Pick[]>(res, 'Could not load profile')
}

export async function addPick(token: string, movieId: number): Promise<void> {
  const res = await fetch('/api/me/picks', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ movieId }),
  })
  await handleVoid(res, 'Could not save pick')
}

export async function fetchMyProfileCard(token: string): Promise<ProfileCard> {
  const res = await fetch('/api/me/profile-card', {
    headers: { Authorization: `Bearer ${token}` },
  })
  return handle<ProfileCard>(res, 'Could not load profile')
}

/** Public profile header; pass token so the API can return is_following for the viewer. */
export async function fetchProfileByNick(nick: string, token: string | null): Promise<ProfileCard> {
  const res = await fetch(`/api/users/by-nick/${encodeURIComponent(nick)}/profile`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  return handle<ProfileCard>(res, 'Could not load profile')
}

export async function followUser(token: string, nick: string): Promise<void> {
  const res = await fetch(`/api/me/follow/${encodeURIComponent(nick)}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  })
  await handleVoid(res, 'Could not follow')
}

export async function unfollowUser(token: string, nick: string): Promise<void> {
  const res = await fetch(`/api/me/follow/${encodeURIComponent(nick)}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  })
  await handleVoid(res, 'Could not unfollow')
}
