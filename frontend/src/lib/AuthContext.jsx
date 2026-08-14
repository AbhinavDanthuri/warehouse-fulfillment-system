import { createContext, useContext, useState } from 'react'
import api from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('wf_user')
    return raw ? JSON.parse(raw) : null
  })

  async function login(email, password) {
    const { data } = await api.post('/auth/login', { email, password })
    localStorage.setItem('wf_token', data.token)
    localStorage.setItem('wf_user', JSON.stringify({ email: data.email, role: data.role }))
    setUser({ email: data.email, role: data.role })
    return data
  }

  function logout() {
    localStorage.removeItem('wf_token')
    localStorage.removeItem('wf_user')
    setUser(null)
  }

  // Mirrors the backend rules so we can hide controls the API would reject.
  // Convenience, not security — the server is still the only thing enforcing.
  const can = {
    manageWarehouses: user?.role === 'ADMIN',
    manageCatalogue: user?.role === 'ADMIN' || user?.role === 'WAREHOUSE_MANAGER',
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, can }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)