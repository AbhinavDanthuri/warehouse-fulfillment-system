import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './lib/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import StockPage from './pages/StockPage'

function RequireAuth({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<RequireAuth><Layout /></RequireAuth>}>
            <Route path="/stock" element={<StockPage />} />
            {/* Warehouses, Products, Orders, and the decision trace land here next. */}
          </Route>
          <Route path="*" element={<Navigate to="/stock" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}