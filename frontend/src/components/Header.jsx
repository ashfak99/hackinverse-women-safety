import React from 'react'
import { Shield, Download, Smartphone, Radio, MapPin, Heart, Github } from 'lucide-react'

export default function Header({ activeTab, setActiveTab }) {
  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-[#FFE1EC] px-4 lg:px-8 py-3.5 transition-all">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        
        {/* Brand Logo & Subtitle */}
        <div 
          onClick={() => setActiveTab('home')}
          className="flex items-center gap-3 cursor-pointer group"
        >
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-[#FF2D78] to-[#FF659A] flex items-center justify-center text-white shadow-md shadow-pink-500/20 group-hover:scale-105 transition-transform">
            <Shield className="w-6 h-6 fill-white/20 stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <h1 className="text-xl font-extrabold tracking-tight bg-gradient-to-r from-[#FF2D78] via-[#E91E63] to-[#880E4F] bg-clip-text text-transparent">
                Nari-Suraksha
              </h1>
              <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-pink-100 text-[#E91E63] border border-pink-200">
                100% Offline
              </span>
            </div>
            <p className="text-xs font-medium text-pink-700/70">
              Safety • Support • Empowerment
            </p>
          </div>
        </div>

        {/* Navigation Tabs (Matching UI Designs) */}
        <nav className="hidden md:flex items-center gap-1 bg-[#FFF0F5] p-1.5 rounded-full border border-[#FFD3E3]">
          {[
            { id: 'home', label: 'Home', icon: Heart },
            { id: 'simulator', label: 'Live Simulator', icon: Radio },
            { id: 'features', label: 'Features & Flow', icon: Smartphone },
            { id: 'safezones', label: 'Safe Zones', icon: MapPin },
          ].map((tab) => {
            const Icon = tab.icon
            const isActive = activeTab === tab.id
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold transition-all ${
                  isActive
                    ? 'bg-[#FF2D78] text-white shadow-sm shadow-pink-500/30 scale-[1.02]'
                    : 'text-gray-600 hover:text-[#FF2D78] hover:bg-pink-50'
                }`}
              >
                <Icon className={`w-3.5 h-3.5 ${isActive ? 'text-white' : 'text-pink-500'}`} />
                {tab.label}
              </button>
            )
          })}
        </nav>

        {/* Action CTAs */}
        <div className="flex items-center gap-2.5">
          <a
            href="https://github.com/Hasnainyt/hackinverse-women-safety"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold text-gray-700 hover:text-black bg-pink-50 hover:bg-pink-100 border border-pink-200 transition-colors"
          >
            <Github className="w-4 h-4" />
            <span>GitHub</span>
          </a>

          <a
            href="https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk"
            download
            className="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-[#FF2D78] to-[#E91E63] hover:from-[#E91E63] hover:to-[#C2185B] shadow-md shadow-pink-500/25 hover:shadow-pink-500/40 transition-all hover:scale-[1.02]"
          >
            <Download className="w-4 h-4" />
            <span>Download APK</span>
          </a>
        </div>

      </div>
    </header>
  )
}
