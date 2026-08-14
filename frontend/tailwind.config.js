/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#14181f',
        slate: { 850: '#2a3341' },
        steel: '#4a5568',
        fog: '#e4e7eb',
        paper: '#f3f4f6',
        hazard: '#f2c230',
        go: '#2e9e6b',
        warn: '#e8a317',
        stop: '#d6453d',
      },
      fontFamily: {
        display: ['Archivo Condensed', 'sans-serif'],
        sans: ['IBM Plex Sans', 'sans-serif'],
        mono: ['IBM Plex Mono', 'monospace'],
      },
    },
  },
  plugins: [],
}