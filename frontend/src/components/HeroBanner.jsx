import React from 'react'
import { Shield, ArrowRight, Radio, Heart, Sparkles, AlertCircle } from 'lucide-react'

export default function HeroBanner({ onOpenSimulator }) {
  return (
    <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#FFE7F1] via-[#FFF0F6] to-[#FFE0ED] border border-[#FFD0E2] p-6 lg:p-10 shadow-soft-pink">
      
      {/* Decorative background aura & floating hearts */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-pink-300/30 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-10 -left-10 w-72 h-72 bg-pink-400/20 rounded-full blur-2xl pointer-events-none" />
      
      <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
        
        {/* Left Column: Heading & CTAs */}
        <div className="lg:col-span-7 space-y-5">
          
          {/* Tag Pill */}
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/90 border border-pink-200 shadow-sm">
            <Shield className="w-4 h-4 text-[#FF2D78] fill-pink-100" />
            <span className="text-xs font-bold text-[#FF2D78] tracking-wide">
              Women's Safety & Offline BLE Mesh
            </span>
            <span className="w-1.5 h-1.5 rounded-full bg-[#FF2D78] animate-ping" />
          </div>

          {/* Main Title matching the screenshot */}
          <div>
            <h2 className="text-4xl sm:text-5xl lg:text-6xl font-black text-[#2D1522] tracking-tight leading-[1.1]">
              Your Safety <br />
              <span className="bg-gradient-to-r from-[#FF2D78] via-[#E91E63] to-[#880E4F] bg-clip-text text-transparent inline-flex items-center gap-3">
                Our Priority
                <Heart className="w-9 h-9 text-[#FF2D78] fill-pink-200 inline stroke-[2.5]" />
              </span>
            </h2>
            <p className="mt-4 text-base sm:text-lg text-gray-700 max-w-xl font-medium leading-relaxed">
              Stay safe. Stay connected. <span className="text-[#FF2D78] font-bold">Network gayab, fir bhi help alive.</span> An offline guardian that broadcasts distress signals peer-to-peer without internet, cell towers, or cloud servers.
            </p>
          </div>

          {/* Hero Buttons matching UI design */}
          <div className="flex flex-wrap items-center gap-3.5 pt-2">
            <button
              onClick={onOpenSimulator}
              className="inline-flex items-center gap-2.5 px-6 py-3.5 rounded-2xl text-sm font-extrabold text-white bg-gradient-to-r from-[#FF2D78] to-[#E91E63] hover:from-[#E91E63] hover:to-[#C2185B] shadow-lg shadow-pink-500/30 hover:shadow-pink-500/50 hover:scale-[1.02] transition-all"
            >
              <span className="px-2 py-0.5 rounded-md bg-white/20 text-xs font-black">SOS</span>
              <span>Tap for Immediate Help Demo</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <a
              href="https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk"
              download
              className="inline-flex items-center gap-2 px-5 py-3.5 rounded-2xl text-sm font-bold text-[#E91E63] bg-white hover:bg-pink-50 border border-pink-200 shadow-sm transition-all"
            >
              <span>Get Android App (18.4MB)</span>
            </a>
          </div>

          {/* Quick Value Metrics */}
          <div className="grid grid-cols-3 gap-3 pt-4 max-w-md">
            <div className="bg-white/70 backdrop-blur-sm rounded-2xl p-3 border border-pink-100 text-center">
              <p className="text-xl font-black text-[#FF2D78]">0 MB</p>
              <p className="text-[11px] font-bold text-gray-600">Internet Needed</p>
            </div>
            <div className="bg-white/70 backdrop-blur-sm rounded-2xl p-3 border border-pink-100 text-center">
              <p className="text-xl font-black text-[#FF2D78]">5 Sec</p>
              <p className="text-[11px] font-bold text-gray-600">Cancel Window</p>
            </div>
            <div className="bg-white/70 backdrop-blur-sm rounded-2xl p-3 border border-pink-100 text-center">
              <p className="text-xl font-black text-[#FF2D78]">15 Byte</p>
              <p className="text-[11px] font-bold text-gray-600">Fast BLE Packet</p>
            </div>
          </div>

        </div>

        {/* Right Column: Woman Silhouette Art Illustration matching Screenshot */}
        <div className="lg:col-span-5 flex justify-center relative">
          
          <div className="relative w-full max-w-[340px] aspect-square flex items-center justify-center">
            
            {/* Pulsating background rings */}
            <div className="absolute inset-0 rounded-full bg-gradient-to-tr from-pink-400/30 to-pink-200/20 blur-xl animate-pulse" />
            
            {/* Outer Shield Outline */}
            <div className="relative w-72 h-80 rounded-[40px] bg-gradient-to-b from-white via-pink-50 to-pink-100/80 p-6 border-2 border-pink-200 shadow-xl flex flex-col items-center justify-center text-center overflow-hidden">
              
              {/* Floating Heart accents */}
              <div className="absolute top-4 right-6 text-pink-400 animate-bounce">
                <Heart className="w-5 h-5 fill-pink-400" />
              </div>
              <div className="absolute bottom-12 left-4 text-pink-300">
                <Heart className="w-4 h-4 fill-pink-300" />
              </div>

              {/* Central Illustrated Silhouette SVG */}
              <div className="w-36 h-36 rounded-full bg-gradient-to-tr from-[#FF2D78] to-[#FF85AC] flex items-center justify-center p-1 shadow-lg shadow-pink-500/30">
                <div className="w-full h-full rounded-full bg-[#FFE7F1] flex items-center justify-center overflow-hidden relative">
                  <svg viewBox="0 0 100 100" className="w-28 h-28 text-[#FF2D78] fill-current">
                    <path d="M50 20C40 20 32 28 32 38C32 46 38 52 45 55C36 58 28 66 26 78H74C72 66 64 58 55 55C62 52 68 46 68 38C68 28 60 20 50 20ZM50 26C56.6 26 62 31.4 62 38C62 44.6 56.6 50 50 50C43.4 50 38 44.6 38 38C38 31.4 43.4 26 50 26Z" />
                  </svg>
                </div>
              </div>

              <div className="mt-4">
                <span className="text-xs font-bold uppercase tracking-wider text-pink-600 bg-pink-100/90 px-3 py-1 rounded-full border border-pink-200">
                  Always Protected
                </span>
                <h4 className="text-lg font-black text-[#2D1522] mt-2">
                  Safety Builds Confidence
                </h4>
                <p className="text-xs text-gray-500 font-medium">
                  Peer-to-peer silent guardian
                </p>
              </div>

            </div>

          </div>

        </div>

      </div>
    </section>
  )
}
