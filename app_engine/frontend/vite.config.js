// app_engine/frontend/vite.config.js (Corrigido)

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react( )],
  
  // Define o caminho base público para a aplicação.
  // Corresponde à estrutura de diretórios no servidor.
  base: '/mcp/meshwave/project_c3/',
})

