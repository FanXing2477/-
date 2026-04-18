import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: '',
    username: '',
    nickname: '',
    avatar: '',
    email: '',
    phone: '',
    motto: '',
    token: ''
  }),

  actions: {
    setUser(user) {
      this.id = user.id
      this.username = user.username
      this.nickname = user.nickname || ''
      this.avatar = user.avatar || ''
      this.email = user.email || ''
      this.phone = user.phone || ''
      this.motto = user.motto || ''
      this.token = user.token
      localStorage.setItem('token', user.token)
    },
    logout() {
      this.$reset()
      localStorage.removeItem('token')
    }
  },

  persist: true
})