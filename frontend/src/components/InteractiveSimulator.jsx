import React, { useState, useEffect } from 'react'
import { AlertCircle, Radio, Shield, CheckCircle, Volume2, Flashlight, Calculator, RefreshCw, Smartphone, MapPin, Heart, Signal } from 'lucide-react'

export default function InteractiveSimulator() {
  const [activeMode, setActiveMode] = useState('sos') // 'sos' | 'decoy'
  
  // Phone A (Sender) State
  const [sosState, setSosState] = useState('idle') // 'idle' | 'countdown' | 'broadcasting'
  const [countdown, setCountdown] = useState(5)
  const [responderCount, setResponderCount] = useState(0)

  // Phone B (Receiver) State
  const [receivedAlert, setReceivedAlert] = useState(null)
  const [isAckSent, setIsAckSent] = useState(false)
  const [isSirenOn, setIsSirenOn] = useState(false)
  const [isTorchOn, setIsTorchOn] = useState(false)

  // Decoy Calculator State
  const [calcDisplay, setCalcDisplay] = useState('0')
  const [calcInputHistory, setCalcInputHistory] = useState('')
  const [secretPin, setSecretPin] = useState('1234')
  const [secretUnlocked, setSecretUnlocked] = useState(false)

  // Countdown timer logic for Phone A
  useEffect(() => {
    let timer
    if (sosState === 'countdown') {
      if (countdown > 0) {
        timer = setTimeout(() => setCountdown((prev) => prev - 1), 1000)
      } else {
        setSosState('broadcasting')
        // Automatically simulate Phone B receiving after 1.5s
        setTimeout(() => {
          setReceivedAlert({
            senderId: '#7842',
            timestamp: new Date().toLocaleTimeString(),
            lat: 28.6139,
            lon: 77.2090,
            rssi: -58,
            distanceText: 'Approx. < 3 meters away (Immediate Proximity)',
            category: 'VERY CLOSE'
          })
        }, 1200)
      }
    }
    return () => clearTimeout(timer)
  }, [sosState, countdown])

  const handleTriggerSos = () => {
    setSosState('countdown')
    setCountdown(5)
    setResponderCount(0)
    setIsAckSent(false)
    setReceivedAlert(null)
  }

  const handleCancelCountdown = () => {
    setSosState('idle')
    setCountdown(5)
  }

  const handleResetSimulator = () => {
    setSosState('idle')
    setCountdown(5)
    setResponderCount(0)
    setReceivedAlert(null)
    setIsAckSent(false)
    setIsSirenOn(false)
    setIsTorchOn(false)
    setCalcDisplay('0')
    setCalcInputHistory('')
    setSecretUnlocked(false)
  }

  const handleSendAck = () => {
    setIsAckSent(true)
    setResponderCount((prev) => prev + 1)
  }

  // Calculator interaction
  const handleCalcBtn = (val) => {
    if (val === 'C') {
      setCalcDisplay('0')
      setCalcInputHistory('')
      return
    }
    if (val === '=') {
      if (calcDisplay === secretPin || calcInputHistory.endsWith(secretPin)) {
        setActiveMode('sos')
        handleTriggerSos()
        return
      }
      try {
        const clean = calcDisplay.replace(/×/g, '*').replace(/÷/g, '/')
        if (/^[0-9+\-*/. %()]+$/.test(clean)) {
          // Safe mathematical expression execution
          const res = new Function(`return (${clean})`)()
          setCalcDisplay(String(res))
        } else {
          setCalcDisplay('Error')
        }
      } catch (e) {
        setCalcDisplay('Error')
      }
      return
    }
    setCalcInputHistory((prev) => prev + val)
    if (calcDisplay === '0' || calcDisplay === 'Error') {
      setCalcDisplay(val)
    } else {
      setCalcDisplay((prev) => prev + val)
    }
  }

  return (
    <section className="bg-white rounded-3xl p-6 lg:p-8 border border-pink-200 shadow-soft-pink space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-pink-100">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-[#FF2D78] animate-pulse" />
            <h3 className="text-2xl font-black text-[#2D1522]">
              Interactive 2-Phone BLE Broadcast Simulator
            </h3>
            <span className="text-[10px] uppercase font-bold tracking-wider px-2.5 py-0.5 rounded-full bg-amber-100 text-amber-800 border border-amber-300">
              Illustrative Demo • Not Real BLE
            </span>
          </div>
          <p className="text-xs sm:text-sm text-gray-600 font-medium mt-1">
            Simulate single-hop offline SOS broadcast, Decoy Calculator PIN trigger, and 2-way Responder ACK live in your browser.
            <span className="block text-gray-500 text-[11px] mt-0.5 italic">
              *Illustrative simulation only: web browsers cannot transmit native BLE packets. Download and install the Android APK for authentic offline Bluetooth Low Energy radio transmission.
            </span>
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex bg-pink-50 p-1 rounded-xl border border-pink-200">
            <button
              onClick={() => setActiveMode('sos')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeMode === 'sos' ? 'bg-[#FF2D78] text-white shadow-sm' : 'text-gray-600 hover:text-pink-600'
              }`}
            >
              Standard SOS Mode
            </button>
            <button
              onClick={() => setActiveMode('decoy')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeMode === 'decoy' ? 'bg-[#FF2D78] text-white shadow-sm' : 'text-gray-600 hover:text-pink-600'
              }`}
            >
              Decoy Calculator Mode
            </button>
          </div>

          <button
            onClick={handleResetSimulator}
            className="p-2 rounded-xl text-gray-500 hover:text-[#FF2D78] hover:bg-pink-50 border border-pink-200 transition-colors"
            title="Reset Simulator"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* 2-Device Interactive Playground */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
        
        {/* ================= DEVICE 1 (PHONE A: SENDER) ================= */}
        <div className="bg-gradient-to-b from-[#FFF5F8] to-white rounded-3xl p-6 border-2 border-pink-200 shadow-md space-y-4">
          
          <div className="flex items-center justify-between pb-3 border-b border-pink-100">
            <div className="flex items-center gap-2">
              <Smartphone className="w-5 h-5 text-[#FF2D78]" />
              <h4 className="text-sm font-extrabold text-[#2D1522]">Phone A (Distress Sender)</h4>
            </div>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-pink-100 text-[#FF2D78] border border-pink-200">
              {activeMode === 'decoy' ? 'Disguised as Calculator' : 'Airplane Mode + BLE ON'}
            </span>
          </div>

          {activeMode === 'decoy' ? (
            /* Decoy Calculator Interface */
            <div className="bg-[#1E1E1E] rounded-2xl p-4 text-white shadow-inner max-w-[280px] mx-auto space-y-3">
              <div className="text-right">
                <p className="text-[10px] text-gray-400">Calculator Disguise</p>
                <div className="text-3xl font-light tracking-wider overflow-x-auto py-1 font-mono">
                  {calcDisplay}
                </div>
              </div>

              <div className="grid grid-cols-4 gap-2 text-sm font-semibold">
                {['C', '⌫', '%', '÷', '7', '8', '9', '×', '4', '5', '6', '-', '1', '2', '3', '+'].map((btn) => (
                  <button
                    key={btn}
                    onClick={() => handleCalcBtn(btn)}
                    className="h-10 rounded-full bg-[#333333] hover:bg-[#444444] active:scale-95 flex items-center justify-center transition-all"
                  >
                    {btn}
                  </button>
                ))}
                <button
                  onClick={() => handleCalcBtn('0')}
                  className="col-span-2 h-10 rounded-full bg-[#333333] hover:bg-[#444444] active:scale-95 flex items-center justify-center transition-all"
                >
                  0
                </button>
                <button
                  onClick={() => handleCalcBtn('.')}
                  className="h-10 rounded-full bg-[#333333] hover:bg-[#444444] active:scale-95 flex items-center justify-center transition-all"
                >
                  .
                </button>
                <button
                  onClick={() => handleCalcBtn('=')}
                  className="h-10 rounded-full bg-[#FF2D78] hover:bg-pink-600 active:scale-95 text-white flex items-center justify-center font-bold transition-all shadow-md shadow-pink-500/40"
                >
                  =
                </button>
              </div>

              <div className="text-center pt-2">
                <span className="text-[11px] text-pink-300 font-medium">
                  💡 Tip: Enter PIN <strong className="text-white">1234</strong> then press <strong className="text-white">=</strong> to fire secret SOS!
                </span>
              </div>
            </div>
          ) : (
            /* Standard SOS Trigger Interface */
            <div className="flex flex-col items-center justify-center py-6 text-center space-y-4">
              
              {sosState === 'idle' && (
                <>
                  <div className="relative">
                    <div className="w-36 h-36 rounded-full bg-pink-100 flex items-center justify-center animate-pulse-ring" />
                    <button
                      onClick={handleTriggerSos}
                      className="absolute inset-0 m-auto w-32 h-32 rounded-full bg-gradient-to-tr from-[#FF2D78] to-[#E91E63] text-white shadow-xl shadow-pink-500/40 hover:scale-105 active:scale-95 transition-all flex flex-col items-center justify-center"
                    >
                      <AlertCircle className="w-10 h-10" />
                      <span className="text-xs font-black tracking-wider mt-1">TAP SOS</span>
                    </button>
                  </div>
                  <p className="text-xs text-gray-500 font-medium">
                    Or simulate 3 rapid shakes with your phone accelerometer
                  </p>
                </>
              )}

              {sosState === 'countdown' && (
                <div className="space-y-3 w-full max-w-xs">
                  <div className="w-24 h-24 rounded-full bg-pink-600 text-white flex items-center justify-center text-4xl font-black mx-auto shadow-lg shadow-pink-600/40 animate-bounce">
                    {countdown}s
                  </div>
                  <h5 className="text-sm font-extrabold text-[#E91E63]">
                    Emergency Beacon Initiating in {countdown}s...
                  </h5>
                  <p className="text-xs text-gray-600">
                    Capturing GPS coordinates & recording microphone evidence.
                  </p>
                  <button
                    onClick={handleCancelCountdown}
                    className="w-full py-2.5 rounded-xl bg-gray-900 hover:bg-black text-white text-xs font-bold transition-all shadow-md"
                  >
                    I'M SAFE (CANCEL SOS)
                  </button>
                </div>
              )}

              {sosState === 'broadcasting' && (
                <div className="space-y-4 w-full">
                  <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-100 text-red-700 text-xs font-bold animate-pulse">
                    <Radio className="w-3.5 h-3.5" />
                    <span>Broadcasting 15-byte BLE Beacon (60s)</span>
                  </div>

                  {/* Live Responder ACK Counter Card */}
                  <div className={`p-4 rounded-2xl border transition-all ${
                    responderCount > 0
                      ? 'bg-emerald-50 border-emerald-300 text-emerald-900 shadow-md'
                      : 'bg-pink-50 border-pink-200 text-pink-900'
                  }`}>
                    <div className="flex items-center gap-3">
                      <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                        responderCount > 0 ? 'bg-emerald-600 text-white' : 'bg-pink-500 text-white'
                      }`}>
                        <Shield className="w-5 h-5" />
                      </div>
                      <div className="text-left">
                        <p className="text-xs font-black">
                          {responderCount > 0
                            ? `👥 ${responderCount} Nearby Responder(s) Alerted!`
                            : 'Listening for Nearby Peer Responders...'}
                        </p>
                        <p className="text-[11px] opacity-80">
                          {responderCount > 0
                            ? 'A nearby person has acknowledged your alert.'
                            : 'Signal transmitting offline across nearby BLE radius.'}
                        </p>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={handleResetSimulator}
                    className="px-5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-sm transition-all"
                  >
                    I AM SAFE (STOP BROADCAST)
                  </button>
                </div>
              )}

            </div>
          )}

        </div>

        {/* ================= DEVICE 2 (PHONE B: RESPONDER) ================= */}
        <div className="bg-gradient-to-b from-[#FFF5F8] to-white rounded-3xl p-6 border-2 border-pink-200 shadow-md space-y-4">
          
          <div className="flex items-center justify-between pb-3 border-b border-pink-100">
            <div className="flex items-center gap-2">
              <Smartphone className="w-5 h-5 text-emerald-600" />
              <h4 className="text-sm font-extrabold text-[#2D1522]">Phone B (Nearby Responder)</h4>
            </div>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 border border-emerald-200">
              Background Guardian Active
            </span>
          </div>

          {receivedAlert ? (
            /* Received Distress Alert Card */
            <div className="space-y-3.5 animate-fadeIn">
              
              <div className="bg-gradient-to-r from-red-600 to-pink-600 rounded-2xl p-4 text-white shadow-lg shadow-red-500/20">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <AlertCircle className="w-5 h-5 animate-bounce" />
                    <span className="text-xs font-black uppercase tracking-wider">
                      🚨 DISTRESS SIGNAL DETECTED
                    </span>
                  </div>
                  <span className="text-[10px] font-mono bg-white/20 px-2 py-0.5 rounded">
                    {receivedAlert.timestamp}
                  </span>
                </div>
                <p className="text-xs font-medium text-white/90 mt-1">
                  Anonymous Sender: <span className="font-bold">{receivedAlert.senderId}</span>
                </p>
              </div>

              {/* RSSI Distance Bucket */}
              <div className="bg-white rounded-2xl p-3.5 border border-pink-200 shadow-sm space-y-1">
                <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                  <span>Proximity Estimate</span>
                  <span className="text-pink-600 font-mono flex items-center gap-1">
                    <Signal className="w-3.5 h-3.5" />
                    {receivedAlert.rssi} dBm
                  </span>
                </div>
                <p className="text-lg font-black text-emerald-600">
                  {receivedAlert.distanceText}
                </p>
              </div>

              {/* GPS Coordinates */}
              <div className="bg-white rounded-2xl p-3.5 border border-pink-200 shadow-sm space-y-1">
                <div className="flex items-center gap-1.5 text-xs font-bold text-gray-500">
                  <MapPin className="w-3.5 h-3.5 text-red-500" />
                  <span>Offline Captured Coordinates</span>
                </div>
                <p className="text-xs font-mono font-bold text-gray-800">
                  Lat: {receivedAlert.lat}, Lon: {receivedAlert.lon}
                </p>
              </div>

              {/* Responder 2-Way ACK Action */}
              <button
                onClick={handleSendAck}
                disabled={isAckSent}
                className={`w-full py-3 rounded-2xl text-xs font-black tracking-wide shadow-md transition-all flex items-center justify-center gap-2 ${
                  isAckSent
                    ? 'bg-emerald-700 text-white cursor-default'
                    : 'bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white shadow-emerald-500/25 active:scale-[0.98]'
                }`}
              >
                <CheckCircle className="w-4 h-4" />
                <span>{isAckSent ? '✓ ACK TRANSMITTED TO SENDER' : 'I AM RESPONDING (SEND ACK BEACON)'}</span>
              </button>

              {/* Responder Helper Tools */}
              <div className="grid grid-cols-2 gap-2 pt-1">
                <button
                  onClick={() => setIsSirenOn(!isSirenOn)}
                  className={`py-2 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 border transition-all ${
                    isSirenOn ? 'bg-red-600 text-white border-red-700' : 'bg-white text-gray-700 border-pink-200 hover:bg-pink-50'
                  }`}
                >
                  <Volume2 className="w-3.5 h-3.5" />
                  <span>{isSirenOn ? 'Siren Playing' : 'Deterrent Siren'}</span>
                </button>

                <button
                  onClick={() => setIsTorchOn(!isTorchOn)}
                  className={`py-2 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 border transition-all ${
                    isTorchOn ? 'bg-amber-500 text-white border-amber-600' : 'bg-white text-gray-700 border-pink-200 hover:bg-pink-50'
                  }`}
                >
                  <Flashlight className="w-3.5 h-3.5" />
                  <span>{isTorchOn ? 'Torch Active' : 'Strobe Flashlight'}</span>
                </button>
              </div>

            </div>
          ) : (
            /* Standby State */
            <div className="flex flex-col items-center justify-center py-12 text-center space-y-3">
              <div className="w-16 h-16 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-200">
                <Radio className="w-8 h-8 animate-pulse" />
              </div>
              <h5 className="text-sm font-extrabold text-[#2D1522]">
                Guardian Scanner Listening...
              </h5>
              <p className="text-xs text-gray-500 max-w-xs font-medium">
                When Phone A triggers an SOS, Phone B will immediately ring with heads-up proximity alerts offline.
              </p>
            </div>
          )}

        </div>

      </div>

    </section>
  )
}
