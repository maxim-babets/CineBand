import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { createPost, fetchWall, reactPost } from '../api'
import type { WallPost } from '../types'

export function Feed() {
  const { token, user } = useAuth()
  const [posts, setPosts] = useState<WallPost[]>([])
  const [content, setContent] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setError(null)
    try {
      const data = await fetchWall(token)
      setPosts(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load feed')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    load()
  }, [load])

  async function onPost(e: FormEvent) {
    e.preventDefault()
    if (!token || !user) return
    setError(null)
    try {
      await createPost(token, content)
      setContent('')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Post failed')
    }
  }

  async function onReact(post: WallPost, type: 'LIKE' | 'DISLIKE') {
    if (!token) return
    try {
      await reactPost(token, post.id, type)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Reaction failed')
    }
  }

  return (
    <div className="feed-page">
      <h1 className="page-title">Feed</h1>
      <p className="page-lead">Share what you’re watching — like or pass on posts.</p>

      {user && token ? (
        <form className="compose card" onSubmit={onPost}>
          <textarea
            className="compose-input"
            placeholder="What’s on your mind?"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            maxLength={2000}
            rows={3}
          />
          <button type="submit" className="btn btn--outline" disabled={!content.trim()}>
            Post
          </button>
        </form>
      ) : (
        <p className="card muted">
          <Link to="/login">Sign in</Link> to write on the wall.
        </p>
      )}

      {error && <p className="error">{error}</p>}
      {loading && <p className="muted">Loading…</p>}

      <ul className="post-list">
        {posts.map((p) => (
          <li key={p.id} className="post card">
            <div className="post-head">
              <Link to={`/u/${encodeURIComponent(p.author_nick)}`} className="post-author">
                @{p.author_nick}
              </Link>
              <span className="post-name">{p.author_display_name}</span>
              <time className="post-time" dateTime={p.createdAt}>
                {new Date(p.createdAt).toLocaleString()}
              </time>
            </div>
            <p className="post-body">{p.content}</p>
            <div className="post-actions">
              <span className="post-counts">
                ↑ {p.likes} · ↓ {p.dislikes}
              </span>
              {token && (
                <>
                  <button
                    type="button"
                    className={p.my_reaction === 'LIKE' ? 'react active' : 'react'}
                    onClick={() => onReact(p, 'LIKE')}
                  >
                    Like
                  </button>
                  <button
                    type="button"
                    className={p.my_reaction === 'DISLIKE' ? 'react active dislike' : 'react dislike'}
                    onClick={() => onReact(p, 'DISLIKE')}
                  >
                    Dislike
                  </button>
                </>
              )}
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
