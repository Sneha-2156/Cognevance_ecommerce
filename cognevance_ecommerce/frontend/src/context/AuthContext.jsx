import { createContext, useContext, useState, useCallback } from 'react'
import { api } from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  const persist = (authResponse) => {
    localStorage.setItem('token', authResponse.token)
    const userInfo = { name: authResponse.name, email: authResponse.email, role: authResponse.role }
    localStorage.setItem('user', JSON.stringify(userInfo))
    setUser(userInfo)
  }

  const login = useCallback(async (email, password) => {
    const res = await api.login({ email, password })
    persist(res)
    return res
  }, [])

  const register = useCallback(async (name, email, password) => {
    const res = await api.register({ name, email, password })
    persist(res)
    return res
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAdmin: user?.role === 'ADMIN' }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
