import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { loginRequest, registerRequest } from '../api'
import type { User } from '../types'

const STORAGE_KEY = 'cineband_token'

type AuthContextValue = {
  token: string | null
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (displayName: string, nick: string, email: string, password: string) => Promise<void>
  logout: () => void
  setSession: (token: string, user: User) => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(STORAGE_KEY))
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const setSession = useCallback((t: string, u: User) => {
    localStorage.setItem(STORAGE_KEY, t)
    setToken(t)
    setUser(u)
  }, [])

  useEffect(() => {
    if (!token) {
      setUser(null)
      setLoading(false)
      return
    }
    let cancelled = false
    fetch('/api/me', { headers: { Authorization: `Bearer ${token}` } })
      .then((r) => {
        if (r.status === 401) {
          logout()
          return null
        }
        if (!r.ok) throw new Error('Session check failed')
        return r.json() as Promise<User>
      })
      .then((u) => {
        if (!cancelled && u) setUser(u)
      })
      .catch(() => {
        if (!cancelled) logout()
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [token, logout])

  const login = useCallback(
    async (email: string, password: string) => {
      const res = await loginRequest(email, password)
      setSession(res.access_token, res.user)
    },
    [setSession]
  )

  const register = useCallback(
    async (displayName: string, nick: string, email: string, password: string) => {
      const res = await registerRequest(displayName, nick, email, password)
      setSession(res.access_token, res.user)
    },
    [setSession]
  )

  const value = useMemo(
    () => ({
      token,
      user,
      loading,
      login,
      register,
      logout,
      setSession,
    }),
    [token, user, loading, login, register, logout, setSession]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth outside AuthProvider')
  return ctx
}
