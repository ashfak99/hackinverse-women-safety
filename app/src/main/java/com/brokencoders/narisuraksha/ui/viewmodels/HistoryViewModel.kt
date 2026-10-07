package com.brokencoders.narisuraksha.ui.viewmodels

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brokencoders.narisuraksha.data.SosDao
import com.brokencoders.narisuraksha.data.SosEventEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel(
    private val sosDao: SosDao
) : ViewModel() {

    val events: StateFlow<List<SosEventEntity>> = sosDao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentlyPlayingAudioPath = MutableStateFlow<String?>(null)
    val currentlyPlayingAudioPath: StateFlow<String?> = _currentlyPlayingAudioPath.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(path: String) {
        val file = File(path)
        if (!file.exists()) {
            Log.w(TAG, "Audio file not found at: $path")
            return
        }

        if (_currentlyPlayingAudioPath.value == path) {
            stopAudio()
            return
        }

        stopAudio()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    _currentlyPlayingAudioPath.value = null
                }
                start()
            }
            _currentlyPlayingAudioPath.value = path
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio evidence", e)
            stopAudio()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        } finally {
            mediaPlayer = null
            _currentlyPlayingAudioPath.value = null
        }
    }

    fun deleteEvent(event: SosEventEntity) {
        viewModelScope.launch {
            if (event.audioPath != null) {
                try {
                    File(event.audioPath).delete()
                } catch (e: Exception) {
                    Log.e(TAG, "Could not delete audio file", e)
                }
            }
            sosDao.deleteEvent(event)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            stopAudio()
            sosDao.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }

    companion object {
        private const val TAG = "HistoryViewModel"

        fun provideFactory(sosDao: SosDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(sosDao) as T
            }
        }
    }
}
