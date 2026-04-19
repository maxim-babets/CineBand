import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { fetchPicksByNick, fetchProfileByNick, followUser, unfollowUser } from '../api'
import type { Pick, ProfileCard } from '../types'

export function PublicProfile() {
  const { nick } = useParams<{ nick: string }>()
  const { token, user } = useAuth()
  const [picks, setPicks] = useState<Pick[]>([])
  const [card, setCard] = useState<ProfileCard | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [followBusy, setFollowBusy] = useState(false)

  useEffect(() => {
    if (!nick) return
    setError(null)
    const t = token ?? null
    fetchPicksByNick(nick)
      .then(setPicks)
      .catch((e: Error) => setError(e.message))
    fetchProfileByNick(nick, t)
      .then(setCard)
      .catch(() => setCard(null))
  }, [nick, token])

  async function toggleFollow() {
    if (!token || !nick || !card || card.is_self) return
    setFollowBusy(true)
    try {
      if (card.is_following) {
        await unfollowUser(token, nick)
        setCard({ ...card, is_following: false, follower_count: Math.max(0, card.follower_count - 1) })
      } else {
        await followUser(token, nick)
        setCard({ ...card, is_following: true, follower_count: card.follower_count + 1 })
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not update follow')
    } finally {
      setFollowBusy(false)
    }
  }

  const showFollow =
    token && card && !card.is_self && card.is_following !== null

  return (
    <div className="profile-page">
      <h1 className="page-title">{card?.display_name || `@${nick}`}</h1>
      <p className="profile-handle">@{nick}</p>
      {card && (
        <div className="profile-stats" aria-label="Profile stats">
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
      {showFollow && (
        <div className="profile-follow-row">
          <button
            type="button"
            className={card?.is_following ? 'btn btn--secondary' : 'btn btn--primary'}
            disabled={followBusy}
            onClick={() => void toggleFollow()}
          >
            {card?.is_following ? 'Following' : 'Follow'}
          </button>
        </div>
      )}
      {!token && (
        <p className="signed-meta">
          <Link to="/login">Sign in</Link> to follow this profile and see whether you already follow them.
        </p>
      )}
      {user && nick && user.nick === nick && (
        <p className="signed-meta">This is your public page — same stats as on Profile.</p>
      )}
      <p className="page-lead">Public pick history</p>
      {error && <p className="error">{error}</p>}
      <ul className="pick-list">
        {picks.map((p) => (
          <li key={p.id}>
            <Link to={`/movie/${p.movie_id}`}>{p.title}</Link>
            <span className="pick-time">{new Date(p.moment).toLocaleString()}</span>
          </li>
        ))}
      </ul>
      {picks.length === 0 && !error && <p className="muted">No public picks yet.</p>}
    </div>
  )
}
