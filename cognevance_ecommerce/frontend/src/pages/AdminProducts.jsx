import { useState, useEffect } from 'react'
import { api } from '../api/client.js'

const emptyForm = { name: '', description: '', price: '', stock: '', imageUrl: '', categoryId: '' }

export default function AdminProducts() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [message, setMessage] = useState('')
  const [newCategory, setNewCategory] = useState('')

  const load = async () => {
    try {
      const [prods, cats] = await Promise.all([api.getProducts(), api.getCategories()])
      setProducts(prods)
      setCategories(cats)
    } catch (err) {
      setMessage(err.message)
    }
  }

  useEffect(() => { load() }, [])

  const resetForm = () => {
    setForm(emptyForm)
    setEditingId(null)
  }

  const handleImageUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    setUploading(true)
    try {
      const { imageUrl } = await api.uploadProductImage(file)
      setForm((f) => ({ ...f, imageUrl }))
    } catch (err) {
      setMessage(err.message)
    } finally {
      setUploading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage('')
    const payload = {
      name: form.name,
      description: form.description,
      price: Number(form.price),
      stock: Number(form.stock),
      imageUrl: form.imageUrl,
    }
    try {
      if (editingId) {
        await api.updateProduct(editingId, payload, form.categoryId || null)
        setMessage('Product updated.')
      } else {
        await api.createProduct(payload, form.categoryId || null)
        setMessage('Product created.')
      }
      resetForm()
      load()
    } catch (err) {
      setMessage(err.message)
    }
  }

  const handleEdit = (p) => {
    setEditingId(p.id)
    setForm({
      name: p.name,
      description: p.description || '',
      price: p.price,
      stock: p.stock,
      imageUrl: p.imageUrl || '',
      categoryId: p.category?.id || '',
    })
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this product?')) return
    try {
      await api.deleteProduct(id)
      load()
    } catch (err) {
      setMessage(err.message)
    }
  }

  const handleAddCategory = async () => {
    if (!newCategory.trim()) return
    try {
      await api.createCategory({ name: newCategory.trim() })
      setNewCategory('')
      load()
    } catch (err) {
      setMessage(err.message)
    }
  }

  return (
    <section className="section">
      <h2 className="section__title">Admin — Manage Products</h2>

      <div className="admin-category-row">
        <input placeholder="New category name" value={newCategory}
          onChange={(e) => setNewCategory(e.target.value)} />
        <button className="btn btn--ghost btn--sm" onClick={handleAddCategory}>Add Category</button>
      </div>

      <form className="admin-form" onSubmit={handleSubmit}>
        <h3>{editingId ? 'Edit Product' : 'New Product'}</h3>
        <div className="form-row">
          <label>Name</label>
          <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </div>
        <div className="form-row">
          <label>Description</label>
          <textarea rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div className="form-row-group">
          <div className="form-row">
            <label>Price (₹)</label>
            <input type="number" step="0.01" required value={form.price}
              onChange={(e) => setForm({ ...form, price: e.target.value })} />
          </div>
          <div className="form-row">
            <label>Stock</label>
            <input type="number" required value={form.stock}
              onChange={(e) => setForm({ ...form, stock: e.target.value })} />
          </div>
        </div>
        <div className="form-row">
          <label>Category</label>
          <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            <option value="">None</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="form-row">
          <label>Product Image</label>
          <input type="file" accept="image/*" onChange={handleImageUpload} />
          {uploading && <p className="form-status">Uploading...</p>}
          {form.imageUrl && <img src={form.imageUrl} alt="preview" className="admin-image-preview" />}
        </div>
        <div className="admin-form__actions">
          <button className="btn btn--primary" type="submit">{editingId ? 'Save Changes' : 'Create Product'}</button>
          {editingId && <button type="button" className="btn btn--ghost" onClick={resetForm}>Cancel</button>}
        </div>
        {message && <p className="form-status">{message}</p>}
      </form>

      <h3 style={{ marginTop: 40 }}>Existing Products</h3>
      <table className="admin-table">
        <thead><tr><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th></th></tr></thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.name}</td>
              <td>{p.category?.name || '—'}</td>
              <td>₹{p.price}</td>
              <td>{p.stock}</td>
              <td className="admin-table__actions">
                <button className="btn btn--ghost btn--sm" onClick={() => handleEdit(p)}>Edit</button>
                <button className="btn btn--ghost btn--sm" onClick={() => handleDelete(p.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
