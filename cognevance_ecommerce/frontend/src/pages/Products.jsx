import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client.js'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Products() {
  const { addItem } = useCart()
  const { isAdmin } = useAuth()
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => {})
  }, [])

  const loadProducts = useCallback(async () => {
    setLoading(true)
    try {
      const params = {}
      if (search) params.search = search
      if (categoryId) params.categoryId = categoryId
      setProducts(await api.getProducts(params))
    } catch (err) {
      setMessage(err.message)
    } finally {
      setLoading(false)
    }
  }, [search, categoryId])

  useEffect(() => {
    const t = setTimeout(loadProducts, 300)
    return () => clearTimeout(t)
  }, [loadProducts])

  const handleAdd = (product) => {
    addItem(product, 1)
    setMessage(`Added "${product.name}" to cart.`)
  }

  return (
    <section className="section">
      <h2 className="section__title">Products</h2>

      <div className="filters">
        <input type="text" placeholder="Search products..." value={search}
          onChange={(e) => setSearch(e.target.value)} />
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">All Categories</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      {message && <p className="form-status">{message}</p>}

      {loading ? <p>Loading products...</p> : products.length === 0 ? (
        <p>No products match your search.</p>
      ) : (
        <div className="product-grid">
          {products.map((p) => (
            <div className="product-card" key={p.id}>
              <Link to={`/products/${p.id}`} className="product-card__image">
                {p.imageUrl ? <img src={p.imageUrl} alt={p.name} /> : <div className="product-card__placeholder" />}
              </Link>
              {p.category && <span className="tag">{p.category.name}</span>}
              <h3><Link to={`/products/${p.id}`}>{p.name}</Link></h3>
              <p className="product-card__price">₹{p.price}</p>
              <p className="product-card__stock">{p.stock > 0 ? `${p.stock} in stock` : 'Out of stock'}</p>
              {!isAdmin && (
                <button className="btn btn--primary btn--sm" disabled={p.stock === 0}
                  onClick={() => handleAdd(p)}>
                  Add to Cart
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </section>
  )
}
