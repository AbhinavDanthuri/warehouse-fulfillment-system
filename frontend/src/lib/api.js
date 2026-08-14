import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach the token to every request rather than threading it through each call.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('wf_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// A 401/403 means the token is gone or expired. Clear it and bounce to login
// rather than letting every page render its own broken empty state.
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status
    const onLogin = window.location.pathname === '/login'
    if ((status === 401 || status === 403) && !onLogin) {
      localStorage.removeItem('wf_token')
      localStorage.removeItem('wf_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api