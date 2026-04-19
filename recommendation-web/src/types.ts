export interface Film {
  id: number
  title: string
  genre: string | null
  releaseYear: number | null
  banner_url?: string | null
}

export type Method = 'knn' | 'mf' | 'hybrid'

export interface MovieScore {
  movieId: number
  title: string
  score: number
}

export interface RecommendResponse {
  user_id: number
  method: string
  recommendations: MovieScore[]
}

export interface WatchOption {
  provider: string
  url: string
}

export interface MovieDetail {
  id: number
  title: string
  genre: string | null
  releaseYear: number | null
  banner_url?: string | null
  whereToWatch: WatchOption[]
}

export interface User {
  id: number
  email: string
  nick: string
  display_name: string
}

/** Followers, following, wall posts, and picks (API profile card). */
export interface ProfileCard {
  nick: string
  display_name: string
  follower_count: number
  following_count: number
  posts_count: number
  picks_count: number
  /** Only when viewer is signed in; omitted meaning for anonymous is null from API. */
  is_following: boolean | null
  is_self: boolean
}

export interface AuthResponse {
  access_token: string
  token_type: string
  user: User
}

export interface WallPost {
  id: number
  author_nick: string
  author_display_name: string
  content: string
  /** ISO-8601 from server */
  createdAt: string
  likes: number
  dislikes: number
  my_reaction: string | null
}

export interface Pick {
  id: number
  movie_id: number
  title: string
  moment: string
}

export interface DislikedTitle {
  movie_id: number
  title: string
  at: string
  source: string
}
