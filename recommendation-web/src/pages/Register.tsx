import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [displayName, setDisplayName] = useState('')
  const [nick, setNick] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await register(displayName, nick, email, password)
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <h1 className="auth-title">Join CineBand</h1>
      <p className="auth-lead">Pick a handle (3–32 chars: letters, numbers, underscore).</p>
      <form className="auth-form" onSubmit={onSubmit}>
        <label>
          Display name
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required minLength={1} />
        </label>
        <label>
          Handle
          <input
            value={nick}
            onChange={(e) => setNick(e.target.value)}
            required
            pattern="[a-zA-Z0-9_]{3,32}"
            title="3–32 characters: letters, digits, underscore"
          />
        </label>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Password (min 8)
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" className="btn btn--gold" disabled={loading}>
          {loading ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p className="auth-footer">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </div>
  )
}
