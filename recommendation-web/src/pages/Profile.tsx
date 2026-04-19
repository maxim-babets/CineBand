import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { fetchFeedbackDislikes, fetchMyPicks, fetchMyProfileCard } from '../api'
import type { DislikedTitle, Pick, ProfileCard } from '../types'

export function Profile() {
  const { token, user } = useAuth()
  const [picks, setPicks] = useState<Pick[]>([])
  const [dislikes, setDislikes] = useState<DislikedTitle[]>([])
  const [card, setCard] = useState<ProfileCard | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    Promise.all([fetchMyPicks(token), fetchFeedbackDislikes(token), fetchMyProfileCard(token)])
      .then(([p, d, c]) => {
        if (!cancelled) {
          setPicks(p)
          setDislikes(d)
          setCard(c)
          setError(null)
        }
      })
      .catch((e: Error) => {
        if (!cancelled) setError(e.message)
      })
    return () => {
      cancelled = true
    }
  }, [token])

  if (!user || !token) {
    return (
      <div className="profile-page">
        <h1 className="page-title">Profile</h1>
        <p className="page-lead">
          Sign in to see your stats, picks, and what you’ve passed on — and to get personalized movie
          recommendations from the learning service.
        </p>
        <div className="profile-guest-actions">
          <Link to="/login" className="btn btn--primary">
            Sign in
          </Link>
          <Link to="/register" className="btn btn--secondary">
            Create account
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="profile-page">
      <h1 className="page-title">You</h1>
      <p className="profile-handle">
        @{user.nick || '…'} · {user.display_name}
      </p>
      {card && (
        <div className="profile-stats" aria-label="Your activity">
          <div className="profile-stat">
            <span className="profile-stat-value">{card.follower_count}</span>
            <span className="profile-stat-label">Followers</span>
          </div>
          <div className="profile-stat">
            <span className="profile-stat-value">{card.following_count}</span>
            <span className="profile-stat-label">Following</span>
          </div>
          <div className="profile-stat">
            <span className="profile-stat-value">{card.posts_count}</span>
            <span className="profile-stat-label">Posts</span>
          </div>
          <div className="profile-stat">
            <span className="profile-stat-value">{card.picks_count}</span>
            <span className="profile-stat-label">Picks</span>
          </div>
        </div>
      )}
      <p className="page-lead">
        Public URL:{' '}
        <Link to={`/u/${encodeURIComponent(user.nick)}`} className="profile-link">
          /u/{user.nick}
        </Link>
      </p>

      <h2 className="section-title">Pick history</h2>
      {error && <p className="error">{error}</p>}
      <ul className="pick-list">
        {picks.map((p) => (
          <li key={p.id}>
            <Link to={`/movie/${p.movie_id}`}>{p.title}</Link>
            <span className="pick-time">{new Date(p.moment).toLocaleString()}</span>
          </li>
        ))}
      </ul>
      {picks.length === 0 && !error && <p className="muted small">No picks yet — open a film and tap “Save to my picks”.</p>}

      <h2 className="section-title">Not for me</h2>
      <p className="section-desc">Titles you disliked from surprise or “For you” (used for learning).</p>
      <ul className="pick-list">
        {dislikes.map((d) => (
          <li key={d.movie_id + d.at}>
            <Link to={`/movie/${d.movie_id}`}>{d.title}</Link>
            <span className="pick-time">
              {new Date(d.at).toLocaleString()} · {d.source}
            </span>
          </li>
        ))}
      </ul>
      {dislikes.length === 0 && <p className="muted small">Nothing here yet.</p>}
    </div>
  )
}
