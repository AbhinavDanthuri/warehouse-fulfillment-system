import { useEffect, useState } from 'react'
import api from '../lib/api'
import { useAuth } from '../lib/AuthContext'

export default function StockPage() {
  const { can } = useAuth()
  const [rows, setRows] = useState(null)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('')
  const [editing, setEditing] = useState(null)
  const [draft, setDraft] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    try {
      const { data } = await api.get('/stock')
      setRows(data)
    } catch {
      setError('Could not load stock. Check the backend is running.')
    }
  }

  async function save(row) {
    const quantity = Number(draft)
    if (!Number.isInteger(quantity) || quantity < 0) return
    try {
      await api.put('/stock', {
        warehouseId: row.warehouseId,
        productId: row.productId,
        quantity,
      })
      setEditing(null)
      load()
    } catch {
      setError('That change was rejected. Someone may have edited this row first.')
    }
  }

  const visible = (rows ?? []).filter((r) =>
    `${r.sku} ${r.productName} ${r.warehouseName}`.toLowerCase().includes(filter.toLowerCase())
  )

  const lowCount = (rows ?? []).filter((r) => r.lowStock).length

  return (
    <div className="p-8 max-w-5xl">
      <div className="flex items-baseline justify-between mb-1">
        <h1 className="text-3xl font-bold uppercase">Stock</h1>
        <p className="mono text-xs text-steel">
          {rows ? `${rows.length} rows` : ''}
          {lowCount > 0 && <span className="text-warn"> · {lowCount} at threshold</span>}
        </p>
      </div>
      <p className="text-sm text-steel mb-6">
        Quantities across every warehouse. Edits take the same row lock the
        fulfillment engine uses.
      </p>

      <input
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        placeholder="Filter by SKU, product, or warehouse"
        className="w-full max-w-sm border border-fog bg-white px-3 py-2 mb-6 text-sm"
      />

      {error && (
        <p className="mono text-xs text-stop mb-4 border-l-2 border-stop pl-3">{error}</p>
      )}

      {rows === null && <p className="mono text-xs text-steel">Loading</p>}

      {rows !== null && visible.length === 0 && (
        <p className="text-sm text-steel">
          Nothing matches that filter. Clear it to see all stock rows.
        </p>
      )}

      {visible.length > 0 && (
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b-2 border-ink">
              {['SKU', 'Product', 'Warehouse', 'Qty', 'Threshold', ''].map((h) => (
                <th key={h} className="mono text-[10px] uppercase tracking-wider
                                       text-steel text-left py-2 font-medium">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {visible.map((r) => (
              <tr key={r.id} className="border-b border-fog">
                <td className="mono text-xs py-2">{r.sku}</td>
                <td className="py-2">{r.productName}</td>
                <td className="py-2 text-steel">{r.warehouseName}</td>
                <td className="py-2">
                  {editing === r.id ? (
                    <input
                      autoFocus
                      value={draft}
                      onChange={(e) => setDraft(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') save(r)
                        if (e.key === 'Escape') setEditing(null)
                      }}
                      className="mono w-20 border border-ink px-2 py-1"
                    />
                  ) : (
                    <span className={`mono font-semibold ${
                      r.quantity === 0 ? 'text-stop'
                        : r.lowStock ? 'text-warn' : 'text-go'
                    }`}>
                      {r.quantity}
                    </span>
                  )}
                </td>
                <td className="mono text-xs py-2 text-steel">{r.lowStockThreshold}</td>
                <td className="py-2 text-right">
                  {can.manageCatalogue && (
                    editing === r.id ? (
                      <button onClick={() => save(r)}
                        className="mono text-[10px] uppercase text-ink underline">
                        Save
                      </button>
                    ) : (
                      <button
                        onClick={() => { setEditing(r.id); setDraft(String(r.quantity)) }}
                        className="mono text-[10px] uppercase tracking-wider
                                   text-steel hover:text-ink">
                        Edit
                      </button>
                    )
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}