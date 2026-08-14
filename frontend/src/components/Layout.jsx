import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/AuthContext'

const NAV = [
  { to: '/stock', label: 'Stock' },
  { to: '/warehouses', label: 'Warehouses' },
  { to: '/products', label: 'Products' },
  { to: '/orders', label: 'Orders' },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex">
      <aside className="w-56 bg-ink text-white flex flex-col shrink-0">
        <div className="hazard-edge h-2" />

        <div className="px-5 py-6">
          <p className="mono text-[10px] uppercase tracking-widest text-hazard">
            Fulfillment Control
          </p>
          <p className="font-display text-lg uppercase leading-tight mt-1">
            Warehouse Ops
          </p>
        </div>

        <nav className="flex-1 px-2">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `block px-3 py-2 mono text-xs uppercase tracking-wider ${
                  isActive
                    ? 'bg-hazard text-ink font-semibold'
                    : 'text-fog hover:bg-slate-850'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="px-5 py-4 border-t border-slate-850">
          <p className="mono text-[10px] text-steel uppercase">{user?.role}</p>
          <p className="text-xs truncate mb-2">{user?.email}</p>
          <button
            onClick={() => { logout(); navigate('/login') }}
            className="mono text-[10px] uppercase tracking-wider text-hazard hover:underline"
          >
            Sign out
          </button>
        </div>
      </aside>

      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}