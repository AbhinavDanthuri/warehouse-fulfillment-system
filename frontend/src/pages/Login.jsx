import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleSubmit() {
    setError('')
    setBusy(true)
    try {
      await login(email, password)
      navigate('/stock')
    } catch (err) {
      // Say what to do, not just what broke.
      setError(err.response?.status === 401
        ? 'That email and password do not match. Check both and try again.'
        : 'Cannot reach the server. Is the backend running on port 8081?')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="min-h-screen flex">
      <div className="hazard-edge w-3 shrink-0" />

      <div className="flex-1 flex items-center justify-center px-6">
        <div className="w-full max-w-sm">
          <p className="mono text-xs uppercase tracking-widest text-steel mb-2">
            Fulfillment Control
          </p>
          <h1 className="text-4xl font-bold uppercase mb-8 leading-none">
            Warehouse<br />Operations
          </h1>

          <label className="block mono text-xs uppercase tracking-wider text-steel mb-1">
            Email
          </label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
            className="w-full border border-fog bg-white px-3 py-2 mb-4 focus:border-ink"
          />

          <label className="block mono text-xs uppercase tracking-wider text-steel mb-1">
            Password
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
            className="w-full border border-fog bg-white px-3 py-2 mb-6 focus:border-ink"
          />

          {error && (
            <p className="mono text-xs text-stop mb-4 border-l-2 border-stop pl-3">
              {error}
            </p>
          )}

          <button
            onClick={handleSubmit}
            disabled={busy || !email || !password}
            className="w-full bg-ink text-white uppercase font-display tracking-wide py-3
                       hover:bg-slate-850 disabled:opacity-40"
          >
            {busy ? 'Signing in' : 'Sign in'}
          </button>
        </div>
      </div>
    </div>
  )
}