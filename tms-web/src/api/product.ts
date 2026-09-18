import { del, get, post, put } from './request'
import type { PageResult } from './request'
import type { Id, ModelOption, ProductItem } from './types'

export interface ProductQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  category?: string
}

export interface ProductSaveRequest {
  category: string
  name: string
  imagePath?: string
  description?: string
  models: Array<{ id?: Id; model: string }>
}

export const pageProducts = (query: ProductQuery) => get<PageResult<ProductItem>>('/api/products', query)
export const productDetail = (id: Id) => get<ProductItem>(`/api/products/${id}`)
export const createProduct = (data: ProductSaveRequest) => post<Id>('/api/products', data)
export const updateProduct = (id: Id, data: ProductSaveRequest) => put<void>(`/api/products/${id}`, data)
export const deleteProduct = (id: Id) => del<void>(`/api/products/${id}`)
export const exportProducts = (query: ProductQuery) => get<string>('/api/products/export', query)
export const modelOptions = (tenantId?: Id) =>
  get<ModelOption[]>('/api/product-models/options', { tenantId })
