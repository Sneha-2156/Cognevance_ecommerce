const API_BASE = import.meta.env.VITE_API_BASE || '/api'

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = localStorage.getItem('token')
    if (token) headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  const contentType = res.headers.get('content-type') || ''
  const data = contentType.includes('application/json') ? await res.json() : null

  if (!res.ok) {
    const message = data?.error || Object.values(data || {})[0] || 'Request failed'
    throw new Error(message)
  }
  return data
}

async function uploadFile(path, file) {
  const token = localStorage.getItem('token')
  const formData = new FormData()
  formData.append('file', file)

  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data?.error || 'Upload failed')
  return data
}

export const api = {
  register: (payload) => request('/auth/register', { method: 'POST', body: payload, auth: false }),
  login: (payload) => request('/auth/login', { method: 'POST', body: payload, auth: false }),

  getProducts: (params = {}) => {
    const qs = new URLSearchParams(params).toString()
    return request(`/products${qs ? `?${qs}` : ''}`, { auth: false })
  },
  getProduct: (id) => request(`/products/${id}`, { auth: false }),
  createProduct: (payload, categoryId) =>
    request(`/products${categoryId ? `?categoryId=${categoryId}` : ''}`, { method: 'POST', body: payload }),
  updateProduct: (id, payload, categoryId) =>
    request(`/products/${id}${categoryId ? `?categoryId=${categoryId}` : ''}`, { method: 'PUT', body: payload }),
  deleteProduct: (id) => request(`/products/${id}`, { method: 'DELETE' }),
  uploadProductImage: (file) => uploadFile('/upload/image', file),

  getCategories: () => request('/categories', { auth: false }),
  createCategory: (payload) => request('/categories', { method: 'POST', body: payload }),
  deleteCategory: (id) => request(`/categories/${id}`, { method: 'DELETE' }),

  checkout: (payload) => request('/orders/checkout', { method: 'POST', body: payload }),
  myOrders: () => request('/orders/me'),
  allOrders: () => request('/orders'),
  updateOrderStatus: (id, status) => request(`/orders/${id}/status`, { method: 'PUT', body: { status } }),

  confirmPayment: (paymentIntentId) => request('/payments/confirm', { method: 'POST', body: { paymentIntentId } }),

  getDashboard: () => request('/analytics/dashboard'),
}
