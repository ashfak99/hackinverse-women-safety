/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#FFF5F8',
          100: '#FFE4EE',
          200: '#FFCCD $\\rightarrow$ #FFC0D9',
          300: '#FF94BC',
          400: '#FF5C99',
          500: '#FF2D78',
          600: '#E91E63',
          700: '#C2185B',
          800: '#880E4F',
          900: '#4A0829',
        },
        surface: {
          light: '#FFF9FB',
          card: '#FFFFFF',
          soft: '#FDF2F6',
          border: '#FFE1EC',
          dark: '#1A1118'
        }
      },
      boxShadow: {
        'soft-pink': '0 10px 30px -5px rgba(233, 30, 99, 0.12)',
        'glow-pink': '0 0 25px 2px rgba(255, 45, 120, 0.35)',
      }
    },
  },
  plugins: [],
}
