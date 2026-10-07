import React from 'react'
import { Cpu, WifiOff, ShieldCheck, Binary, Activity, Layers } from 'lucide-react'

export default function OfflineArchitecture() {
  const steps = [
    {
      num: '01',
      title: 'Zero-Cloud Trigger',
      desc: 'Activated instantly via 1-tap pulsating UI or 3 accelerometer shakes (>2.7g) with a 5-second false-alarm cancel window.'
    },
    {
      num: '02',
      title: 'Local Evidence & GPS',
      desc: 'MediaRecorder captures encrypted audio to app-private storage while FusedLocationProvider locks GPS coordinates offline.'
    },
    {
      num: '03',
      title: '15-Byte BLE Packetizer',
      desc: 'Encodes senderId (2B), timestamp (4B), lat (4B), lon (4B), and flags (1B) into a 23-byte AD frame under 16-bit Service UUID (0xFDE1).'
    },
    {
      num: '04',
      title: 'P2P Mesh Broadcast & 2-Way ACK',
      desc: 'Nearby phones receive heads-up sirens even screen-off, estimate distance via RSSI, and transmit responder ACK beacons.'
    }
  ]

  return (
    <section className="bg-gradient-to-br from-[#2D1522] via-[#3D182E] to-[#1F0E17] text-white rounded-3xl p-6 lg:p-10 shadow-xl space-y-8">
      
      {/* Section Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs font-black tracking-widest text-[#FF4B8B] uppercase bg-pink-500/10 px-3 py-1 rounded-full border border-pink-500/20">
            Engine & Architecture
          </span>
          <h3 className="text-3xl font-black text-white mt-2">
            Why 100% Offline P2P Beats Cloud SOS Apps
          </h3>
          <p className="text-sm text-pink-200/70 max-w-2xl mt-1">
            Traditional safety apps fail during network outages, basements, or SIM jamming. Nari-Suraksha operates purely on device-level Bluetooth Low Energy radio.
          </p>
        </div>

        <div className="flex items-center gap-3 bg-white/10 px-4 py-2.5 rounded-2xl border border-white/10 backdrop-blur-md">
          <WifiOff className="w-5 h-5 text-[#FF4B8B]" />
          <div>
            <p className="text-xs font-bold text-white">0 Server Reliance</p>
            <p className="text-[10px] text-pink-200">Autonomous Edge Mesh</p>
          </div>
        </div>
      </div>

      {/* Step Cycle Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {steps.map((s) => (
          <div
            key={s.num}
            className="bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-5 transition-all hover:-translate-y-1 backdrop-blur-sm flex flex-col justify-between"
          >
            <div>
              <span className="text-2xl font-black text-[#FF4B8B] opacity-90">{s.num}</span>
              <h4 className="text-base font-extrabold text-white mt-2">{s.title}</h4>
              <p className="text-xs text-pink-100/70 font-medium mt-2 leading-relaxed">{s.desc}</p>
            </div>
            <div className="h-1 w-12 bg-gradient-to-r from-[#FF2D78] to-transparent rounded-full mt-4" />
          </div>
        ))}
      </div>

      {/* 15-Byte Packet Structure Breakdown */}
      <div className="bg-black/40 rounded-2xl p-5 border border-white/10 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Binary className="w-4 h-4 text-[#FF4B8B]" />
            <h5 className="text-xs font-extrabold uppercase tracking-wider text-pink-200">
              Legacy BLE Compliant Packet Spec (15 Bytes Total)
            </h5>
          </div>
          <span className="text-[10px] font-mono bg-pink-500/20 text-pink-300 px-2 py-0.5 rounded border border-pink-500/30">
            16-bit UUID: 0xFDE1 (23B AD Frame)
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 text-center text-xs font-mono">
          <div className="bg-white/10 p-2.5 rounded-xl border border-white/10">
            <p className="text-pink-300 font-bold">senderId</p>
            <p className="text-[10px] text-gray-400">Short (2 Bytes)</p>
          </div>
          <div className="bg-white/10 p-2.5 rounded-xl border border-white/10">
            <p className="text-pink-300 font-bold">timestamp</p>
            <p className="text-[10px] text-gray-400">Epoch Int (4 Bytes)</p>
          </div>
          <div className="bg-white/10 p-2.5 rounded-xl border border-white/10">
            <p className="text-pink-300 font-bold">latitude</p>
            <p className="text-[10px] text-gray-400">Float (4 Bytes)</p>
          </div>
          <div className="bg-white/10 p-2.5 rounded-xl border border-white/10">
            <p className="text-pink-300 font-bold">longitude</p>
            <p className="text-[10px] text-gray-400">Float (4 Bytes)</p>
          </div>
          <div className="bg-white/10 p-2.5 rounded-xl border border-white/10 col-span-2 sm:col-span-1">
            <p className="text-pink-300 font-bold">flags</p>
            <p className="text-[10px] text-gray-400">Bit0:SOS, Bit2:ACK (1B)</p>
          </div>
        </div>
      </div>

    </section>
  )
}
