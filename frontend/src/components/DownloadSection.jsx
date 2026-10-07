import React from 'react'
import { Download, Smartphone, CheckCircle, Shield, QrCode, Github, Sparkles } from 'lucide-react'

export default function DownloadSection() {
  const downloadUrl = "https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk"
  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(downloadUrl)}`

  return (
    <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#FFE7F1] via-[#FFF0F6] to-[#FFE0ED] border-2 border-pink-300 p-6 lg:p-10 shadow-soft-pink space-y-6">
      
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
        
        {/* Left Col: Download details */}
        <div className="lg:col-span-8 space-y-5">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white border border-pink-200 shadow-sm">
            <Sparkles className="w-3.5 h-3.5 text-[#FF2D78]" />
            <span className="text-xs font-bold text-[#FF2D78]">
              Ready-to-Install Android Build (v1.0.0)
            </span>
          </div>

          <div>
            <h3 className="text-3xl sm:text-4xl font-black text-[#2D1522]">
              Download Nari-Suraksha App
            </h3>
            <p className="text-sm sm:text-base text-gray-700 font-medium mt-2 max-w-xl">
              Get the standalone offline guardian app directly on your Android phone. No Google Play Store account or internet connection required after install.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3.5 pt-1">
            <a
              href={downloadUrl}
              download
              className="inline-flex items-center gap-2.5 px-6 py-3.5 rounded-2xl text-sm font-extrabold text-white bg-gradient-to-r from-[#FF2D78] to-[#E91E63] hover:from-[#E91E63] hover:to-[#C2185B] shadow-lg shadow-pink-500/30 hover:shadow-pink-500/50 hover:scale-[1.02] transition-all"
            >
              <Download className="w-5 h-5 stroke-[2.5]" />
              <span>Download APK (18.4 MB)</span>
            </a>

            <a
              href="https://github.com/Hasnainyt/hackinverse-women-safety"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-800 bg-white hover:bg-pink-50 border border-pink-200 shadow-sm transition-all"
            >
              <Github className="w-4 h-4" />
              <span>View Source on GitHub</span>
            </a>
          </div>

          {/* Quick Install Checklist */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
            <div className="flex items-start gap-2 bg-white/70 p-3 rounded-2xl border border-pink-100">
              <CheckCircle className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-xs font-bold text-gray-900">Step 1: Download</p>
                <p className="text-[11px] text-gray-600">Save APK file to phone</p>
              </div>
            </div>
            <div className="flex items-start gap-2 bg-white/70 p-3 rounded-2xl border border-pink-100">
              <CheckCircle className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-xs font-bold text-gray-900">Step 2: Allow Unknown</p>
                <p className="text-[11px] text-gray-600">Toggle allow install</p>
              </div>
            </div>
            <div className="flex items-start gap-2 bg-white/70 p-3 rounded-2xl border border-pink-100">
              <CheckCircle className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-xs font-bold text-gray-900">Step 3: 100% Protected</p>
                <p className="text-[11px] text-gray-600">Works in airplane mode</p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Col: QR Code for 1-Scan Mobile Download */}
        <div className="lg:col-span-4 flex flex-col items-center text-center">
          <div className="bg-white p-4 rounded-3xl border-2 border-pink-200 shadow-lg">
            <img
              src={qrUrl}
              alt="Scan to download Nari-Suraksha APK"
              className="w-40 h-40 rounded-xl"
            />
            <p className="text-xs font-black text-[#2D1522] mt-3">
              Scan with Phone Camera
            </p>
            <p className="text-[10px] text-gray-500 font-medium">
              Direct download on mobile
            </p>
          </div>
        </div>

      </div>

    </section>
  )
}
