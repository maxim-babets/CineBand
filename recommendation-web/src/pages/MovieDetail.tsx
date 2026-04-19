import { useEffect, useMemo, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { addPick, fetchMovieDetail, postRecommendationFeedback, type FeedbackSource } from '../api'
import { useAuth } from '../auth/AuthContext'
import type { MovieDetail as MovieDetailType } from '../types'

function refToSource(ref: string | null): FeedbackSource | null {
  if (ref === 'surprise') return 'SURPRISE'
  if (ref === 'ml') return 'ML_FEED'
  return null
}

export function MovieDetail() {
  const { id } = useParams<{ id: string }>()
  const [searchParams, setSearchParams] = useSearchParams()
  const { token } = useAuth()
  const [movie, setMovie] = useState<MovieDetailType | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [pickMsg, setPickMsg] = useState<string | null>(null)
  const [pickLoading, setPickLoading] = useState(false)
  const [fbMsg, setFbMsg] = useState<string | null>(null)
  const [fbLoading, setFbLoading] = useState(false)

  const feedbackSource = useMemo(
    () => refToSource(searchParams.get('ref')),
    [searchParams]
  )

  useEffect(() => {
    if (!id) return
    const n = Number(id)
    if (Number.isNaN(n)) {
      setError('Invalid movie id')
      setLoading(false)
      return
    }
    let cancelled = false
    setLoading(true)
    fetchMovieDetail(n)
      .then((m) => {
        if (!cancelled) {
          setMovie(m)
          setError(null)
        }
      })
      .catch((e: Error) => {
        if (!cancelled) setError(e.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  async function savePick() {
    if (!token || !movie) return
    setPickLoading(true)
    setPickMsg(null)
    try {
      await addPick(token, movie.id)
      setPickMsg('Saved to your picks.')
    } catch (e) {
      setPickMsg(e instanceof Error ? e.message : 'Could not save')
    } finally {
      setPickLoading(false)
    }
  }

  async function sendFeedback(sentiment: 'LIKE' | 'DISLIKE') {
    if (!token || !movie || !feedbackSource) return
    setFbLoading(true)
    setFbMsg(null)
    try {
      await postRecommendationFeedback(token, movie.id, sentiment, feedbackSource)
      setFbMsg(sentiment === 'LIKE' ? 'Thanks — we’ll lean this way next time.' : 'Noted — saved to your profile & ratings.')
      const next = new URLSearchParams(searchParams)
      next.delete('ref')
      setSearchParams(next, { replace: true })
    } catch (e) {
      setFbMsg(e instanceof Error ? e.message : 'Could not save')
    } finally {
      setFbLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="detail">
        <p className="muted">Loading…</p>
      </div>
    )
  }

  if (error || !movie) {
    return (
      <div className="detail">
        <p className="error">{error ?? 'Not found'}</p>
        <Link to="/" className="back">
          ← Back home
        </Link>
      </div>
    )
  }

  return (
    <div className="detail detail--full">
      <div className="detail-hero">
        {movie.banner_url ? (
          <img src={movie.banner_url} alt="" className="detail-banner" />
        ) : (
          <div className="detail-banner detail-banner--ph" />
        )}
        <div className="detail-hero-overlay" />
      </div>

      <Link to="/" className="back back--on-hero">
        ← Home
      </Link>

      <div className="detail-body">
        <header className="detail-head">
          <h1 className="detail-title">{movie.title}</h1>
          <p className="detail-sub">
            {movie.releaseYear ?? '—'} · {movie.genre ?? 'Genre unknown'}
          </p>
          {token && (
            <div className="detail-actions">
              <button type="button" className="btn btn--outline" onClick={savePick} disabled={pickLoading}>
                {pickLoading ? 'Saving…' : 'Save to my picks'}
              </button>
              {pickMsg && <span className="muted small">{pickMsg}</span>}
            </div>
          )}
          {!token && (
            <p className="muted small">
              <Link to="/login">Sign in</Link> to save picks and train recommendations.
            </p>
          )}
        </header>

        {token && feedbackSource && (
          <section className="learn-card card">
            <h2 className="learn-title">How was this suggestion?</h2>
            <p className="learn-desc">
              {feedbackSource === 'SURPRISE'
                ? 'Your reaction updates your profile and the rating matrix for the next ML run.'
                : 'Feedback from “For you” teaches the hybrid model what to emphasize.'}
            </p>
            <div className="learn-actions">
              <button
                type="button"
                className="btn btn--gold"
                disabled={fbLoading}
                onClick={() => sendFeedback('LIKE')}
              >
                Good pick
              </button>
              <button
                type="button"
                className="btn btn--muted"
                disabled={fbLoading}
                onClick={() => sendFeedback('DISLIKE')}
              >
                Not for me
              </button>
            </div>
            {fbMsg && <p className="small muted">{fbMsg}</p>}
          </section>
        )}

        <section className="watch-section">
          <h2 className="watch-heading">Where to watch</h2>
          <p className="watch-note">
            Links open provider search or home — availability depends on your region.
          </p>
          {movie.whereToWatch.length === 0 ? (
            <p className="muted">No streaming links seeded for this title.</p>
          ) : (
            <ul className="providers">
              {movie.whereToWatch.map((w) => (
                <li key={w.provider + w.url} className="provider-chip">
                  <span className="provider-name">{w.provider}</span>
                  <a href={w.url} target="_blank" rel="noopener noreferrer" className="provider-link">
                    Open
                  </a>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  )
}
