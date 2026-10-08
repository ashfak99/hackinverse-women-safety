import React from 'react'
import { Shield, Heart, Radio, Download } from 'lucide-react'

export default function BottomBar({ onTriggerQuickDemo }) {
  return (
    <div className="fixed bottom-0 inset-x-0 z-30 bg-white/95 backdrop-blur-md border-t border-[#FFE1EC] px-4 py-3 shadow-lg">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        
        {/* Left message from screenshot */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-pink-100 flex items-center justify-center text-[#FF2D78]">
            <Shield className="w-4 h-4 fill-pink-200 stroke-[2.5]" />
          </div>
          <div>
            <p className="text-xs sm:text-sm font-extrabold text-[#2D1522]">
              You are not alone. Help is just a tap away.
            </p>
            <p className="hidden sm:block text-[11px] text-pink-700/80 font-medium">
              Offline BLE broadcast is active & listening peer-to-peer.
            </p>
          </div>
        </div>

        {/* Right Action Button from screenshot */}
        <div className="flex items-center gap-2">
          <button
            onClick={onTriggerQuickDemo}
            className="flex items-center gap-2 px-5 py-2 rounded-full text-xs font-black text-white bg-gradient-to-r from-[#FF2D78] to-[#E91E63] hover:from-[#E91E63] hover:to-[#C2185B] shadow-md shadow-pink-500/30 hover:scale-105 transition-all"
          >
            <Heart className="w-3.5 h-3.5 fill-white/30" />
            <span>Stay Safe</span>
          </button>
        </div>

      </div>
    </div>
  )
}
