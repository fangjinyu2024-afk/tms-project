import { get, post, put } from './request'
import type { Captcha, LoginResponse, Profile } from './types'

export const fetchCaptcha = () => get<Captcha>('/api/auth/captcha')

export const login = (data: {
  account: string
  password: string
  entry: string
  captchaId: string
  captchaCode: string
}) => post<LoginResponse>('/api/auth/login', data)

export const logout = () => post<void>('/api/auth/logout')

export const fetchProfile = () => get<Profile>('/api/auth/profile')

export const updateProfile = (data: { nickname: string; email?: string; phone?: string }) =>
  put<void>('/api/auth/profile', data)

export const changePassword = (data: { oldPassword: string; newPassword: string }) =>
  post<void>('/api/auth/password', data)

export const forgotPassword = (data: { account: string; captchaId: string; captchaCode: string }) =>
  post<void>('/api/auth/password/forgot', data)

export const resetPassword = (data: { token: string; newPassword: string }) =>
  post<void>('/api/auth/password/reset', data)

export const sendEmailVerify = () => post<void>('/api/auth/email/verify', { action: 'send' })
