import React from 'react'
import { AlertCircle, MapPin, Radio, Users, Calculator, ShieldCheck, ArrowRight, Mic, Activity, Volume2 } from 'lucide-react'

export default function FeatureGrid({ onSelectFeature }) {
  const coreFeatures = [
    {
      id: 'emergency',
      title: 'Emergency',
      subtitle: 'Quick help when you need it most.',
      description: '1-Tap hero button or 3 rapid shakes (> 2.7g) triggers 5-second countdown with immediate audio/vibration feedback.',
      icon: AlertCircle,
      badge: 'Multi-Trigger',
      color: 'from-[#FF2D78] to-[#E91E63]'
    },
    {
      id: 'detection',
      title: 'Detection',
      subtitle: 'Capture & share location and evidence.',
      description: 'Captures fresh device GPS coordinates and starts background audio recording to secure app-private storage.',
      icon: MapPin,
      badge: 'Offline GPS + Mic',
      color: 'from-[#E91E63] to-[#C2185B]'
    },
    {
      id: 'broadcast',
      title: 'Broadcast',
      subtitle: 'Live alert to nearby phones offline.',
      description: 'Encodes 15-byte binary packet under 16-bit Service UUID (0xFDE1) and broadcasts over Bluetooth Low Energy for 60 seconds.',
      icon: Radio,
      badge: 'Peer-to-Peer BLE',
      color: 'from-[#FF4B8B] to-[#FF2D78]'
    },
    {
      id: 'community',
      title: 'Community',
      subtitle: 'Join a safer, stronger network.',
      description: 'Nearby phones receive high-priority heads-up alarms even screen-off, with estimated proximity (<3m, 3-10m, >10m) and 2-way ACK.',
      icon: Users,
      badge: '2-Way ACK Broadcast',
      color: 'from-[#D81B60] to-[#AD1457]'
    }
  ]

  const extraFeatures = [
    {
      id: 'decoy',
      title: 'Decoy Calculator Disguise',
      subtitle: 'Hidden in Plain Sight',
      description: 'A fully working arithmetic calculator that covertly triggers SOS when entering secret PIN followed by = or unlocks real guardian UI on long-press =.',
      icon: Calculator,
      tag: 'Anti-Tamper'
    },
    {
      id: 'safezones',
      title: 'Offline Safe Zones & Help Desks',
      subtitle: 'Verified Safety Havens',
      description: 'Pre-cached offline directory of 24x7 Women Police Desks, CISF Metro Security Booths, and Trauma Centers with 1-tap emergency dialers.',
      icon: ShieldCheck,
      tag: 'Offline Directory'
    }
  ]

  return (
    <div className="space-y-6">
      
      {/* Section Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-2xl font-black text-[#2D1522] tracking-tight">
            How Nari-Suraksha Protects You
          </h3>
          <p className="text-xs sm:text-sm font-medium text-gray-600">
            A comprehensive 4-stage offline safety cycle built into every device.
          </p>
        </div>
        <span className="hidden sm:inline-flex text-xs font-bold text-[#FF2D78] bg-pink-100 px-3 py-1 rounded-full border border-pink-200">
          Core Workstreams
        </span>
      </div>

      {/* 4 Core Cards from Screenshot */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {coreFeatures.map((f) => {
          const Icon = f.icon
          return (
            <div
              key={f.id}
              onClick={() => onSelectFeature && onSelectFeature(f.id)}
              className="group bg-white rounded-3xl p-5 border border-[#FFE1EC] hover:border-pink-300 shadow-soft-pink hover:shadow-glow-pink transition-all duration-300 flex flex-col justify-between cursor-pointer hover:-translate-y-1"
            >
              <div>
                {/* Icon Circle matching Screenshot */}
                <div className="flex items-center justify-between mb-4">
                  <div className={`w-12 h-12 rounded-2xl bg-gradient-to-tr ${f.color} flex items-center justify-center text-white shadow-md shadow-pink-500/20 group-hover:scale-110 transition-transform`}>
                    <Icon className="w-6 h-6 stroke-[2.2]" />
                  </div>
                  <span className="text-[10px] font-extrabold text-[#E91E63] bg-pink-50 px-2 py-0.5 rounded-full border border-pink-100">
                    {f.badge}
                  </span>
                </div>

                <h4 className="text-lg font-extrabold text-[#2D1522] group-hover:text-[#FF2D78] transition-colors">
                  {f.title}
                </h4>
                <p className="text-xs font-bold text-pink-700/80 mt-0.5">
                  {f.subtitle}
                </p>
                <p className="text-xs text-gray-600 font-medium mt-2 leading-relaxed">
                  {f.description}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-pink-50 flex items-center justify-between text-[#FF2D78]">
                <span className="text-xs font-bold group-hover:underline">Explore Flow</span>
                <div className="w-7 h-7 rounded-full bg-pink-50 flex items-center justify-center group-hover:bg-[#FF2D78] group-hover:text-white transition-colors">
                  <ArrowRight className="w-3.5 h-3.5" />
                </div>
              </div>
            </div>
          )
        })}
      </div>

      {/* Extra Innovation Features */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
        {extraFeatures.map((f) => {
          const Icon = f.icon
          return (
            <div
              key={f.id}
              onClick={() => onSelectFeature && onSelectFeature(f.id)}
              className="bg-gradient-to-r from-white to-[#FFF5F8] rounded-3xl p-5 border border-pink-200 hover:border-pink-300 shadow-sm flex items-center gap-4 cursor-pointer hover:shadow-soft-pink transition-all"
            >
              <div className="w-14 h-14 rounded-2xl bg-[#FFE4EE] text-[#FF2D78] flex-shrink-0 flex items-center justify-center shadow-inner">
                <Icon className="w-7 h-7 stroke-[2]" />
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-extrabold text-[#2D1522]">
                    {f.title}
                  </h4>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-pink-100 text-[#E91E63]">
                    {f.tag}
                  </span>
                </div>
                <p className="text-xs text-gray-600 font-medium mt-1">
                  {f.description}
                </p>
              </div>
              <ArrowRight className="w-5 h-5 text-pink-400 group-hover:text-pink-600" />
            </div>
          )
        })}
      </div>

    </div>
  )
}
