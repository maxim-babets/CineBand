import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Shell() {
  const { user, logout, loading } = useAuth()

  const initials = user?.display_name
    ? user.display_name
        .split(/\s+/)
        .map((s) => s[0])
        .join('')
        .slice(0, 2)
        .toUpperCase()
    : '?'

  return (
    <div className="shell">
      <header className="topnav">
        <div className="topnav-inner">
          <Link to="/" className="brand">
            <span className="brand-mark">CB</span>
            <span className="brand-text">CineBand</span>
          </Link>

          <nav className="nav-main" aria-label="Primary">
            <NavLink to="/" className={({ isActive }) => 'nav-pill' + (isActive ? ' nav-pill--active' : '')} end>
              Home
            </NavLink>
            <NavLink to="/feed" className={({ isActive }) => 'nav-pill' + (isActive ? ' nav-pill--active' : '')}>
              Feed
            </NavLink>
            <NavLink to="/me" className={({ isActive }) => 'nav-pill' + (isActive ? ' nav-pill--active' : '')}>
              Profile
            </NavLink>
          </nav>

          <div className="nav-user-area">
            {!loading && user ? (
              <>
                <div className="user-chip" title={user.email}>
                  <span className="user-avatar">{initials}</span>
                  <span className="user-nick">@{user.nick || user.id}</span>
                </div>
                <button type="button" className="btn btn--nav-signout" onClick={logout}>
                  Sign out
                </button>
              </>
            ) : (
              !loading && (
                <>
                  <Link to="/login" className="btn btn--nav-login">
                    Sign in
                  </Link>
                  <Link to="/register" className="btn btn--nav-join">
                    Join
                  </Link>
                </>
              )
            )}
          </div>
        </div>
      </header>
      <main className="main-area">
        <Outlet />
      </main>
      <footer className="foot">
        <span>CineBand — share picks, learn taste, find where to watch</span>
      </footer>
    </div>
  )
}
