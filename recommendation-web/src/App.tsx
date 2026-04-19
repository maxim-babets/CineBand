import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { Shell } from './layout/Shell'
import { Feed } from './pages/Feed'
import { Home } from './pages/Home'
import { Login } from './pages/Login'
import { MovieDetail } from './pages/MovieDetail'
import { Profile } from './pages/Profile'
import { PublicProfile } from './pages/PublicProfile'
import { Register } from './pages/Register'
import './App.css'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Shell />}>
            <Route path="/" element={<Home />} />
            <Route path="/feed" element={<Feed />} />
            <Route path="/me" element={<Profile />} />
            <Route path="/u/:nick" element={<PublicProfile />} />
            <Route path="/movie/:id" element={<MovieDetail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
