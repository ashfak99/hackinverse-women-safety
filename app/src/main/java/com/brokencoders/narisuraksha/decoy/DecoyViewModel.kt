package com.brokencoders.narisuraksha.decoy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brokencoders.narisuraksha.data.UserPreferencesRepository
import com.brokencoders.narisuraksha.trigger.SosManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DecoyViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val sosManager: SosManager
) : ViewModel() {

    private val calculatorEngine = CalculatorEngine()

    private val _calculatorState = MutableStateFlow(CalculatorState())
    val calculatorState: StateFlow<CalculatorState> = _calculatorState.asStateFlow()

    private val _secretUnlockedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val secretUnlockedEvent: SharedFlow<Unit> = _secretUnlockedEvent.asSharedFlow()

    private val _decoySosTriggeredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val decoySosTriggeredEvent: SharedFlow<Unit> = _decoySosTriggeredEvent.asSharedFlow()

    fun onDigit(digit: String) {
        _calculatorState.value = calculatorEngine.onDigit(digit)
    }

    fun onDecimal() {
        _calculatorState.value = calculatorEngine.onDecimal()
    }

    fun onOperation(op: String) {
        _calculatorState.value = calculatorEngine.onOperation(op)
    }

    fun onClear() {
        _calculatorState.value = calculatorEngine.onClear()
    }

    fun onBackspace() {
        _calculatorState.value = calculatorEngine.onBackspace()
    }

    fun onEquals() {
        val currentInput = calculatorEngine.getCurrentInputString()

        viewModelScope.launch {
            val secretCode = preferencesRepository.secretDecoyCode.first()

            if (currentInput == secretCode) {
                Log.w(TAG, "Secret SOS passcode entered in Decoy Calculator! Silently initiating SOS trigger...")
                sosManager.triggerSos()
                _decoySosTriggeredEvent.tryEmit(Unit)
            }
        }

        _calculatorState.value = calculatorEngine.onEquals()
    }

    fun onEqualsLongPress() {
        Log.i(TAG, "Secret long-press detected on Decoy Calculator. Unlocking main application.")
        _secretUnlockedEvent.tryEmit(Unit)
    }

    companion object {
        private const val TAG = "DecoyViewModel"

        fun provideFactory(
            preferencesRepository: UserPreferencesRepository,
            sosManager: SosManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DecoyViewModel(preferencesRepository, sosManager) as T
            }
        }
    }
}
