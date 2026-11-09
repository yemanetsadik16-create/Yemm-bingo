package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.example.game.BingoEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.sin

/**
 * High-fidelity, dedicated Amharic Male Caller engine.
 *
 * Rules:
 * - Pure, attractive Amharic male voice exclusively (no English voice fallback for calls).
 * - Plays authentic chime first.
 * - Prioritizes downloaded audio stream or local cache of natural Amharic male speech
 *   (pitched and slowed for authoritative, resonant caller depth).
 * - Selects device TTS Amharic male voice if available with male pitch parameters.
 * - Handles full phonetic phrases (e.g. "ጂ፣ ሃምሳ ዘጠኝ፣ አምስት ዘጠኝ").
 */
class BingoSoundManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isTtsReady = false
    private var isDeviceAmharicSupported = false
    private var mediaPlayer: MediaPlayer? = null

    private var callerPitch: Float = 0.98f // Crisp, resonant, clear male voice
    private var callerSpeed: Float = 1.15f // Fast, clear, snappy caller pace

    private val cacheDir = File(context.cacheDir, "amharic_male_calls_fast_v4").apply {
        if (!exists()) mkdirs()
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupAmharicMaleVoice()
        }
    }

    private fun setupAmharicMaleVoice() {
        val amharicLocale = Locale("am", "ET")
        val available = tts?.isLanguageAvailable(amharicLocale)
        if (available == TextToSpeech.LANG_AVAILABLE ||
            available == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            available == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        ) {
            isDeviceAmharicSupported = true
            tts?.language = amharicLocale

            // Search for dedicated male voice if available in voice set
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val voices = tts?.voices
                    val amharicMaleVoice = voices?.firstOrNull { voice ->
                        voice.locale.language == "am" &&
                                (voice.name.contains("male", ignoreCase = true) ||
                                        voice.name.contains("#male", ignoreCase = true) ||
                                        voice.name.contains("man", ignoreCase = true))
                    } ?: voices?.firstOrNull { it.locale.language == "am" }

                    if (amharicMaleVoice != null) {
                        tts?.voice = amharicMaleVoice
                    }
                } catch (_: Exception) {
                }
            }
        }
        // Warm, natural male caller pitch and relaxed dignified pacing
        tts?.setPitch(callerPitch)
        tts?.setSpeechRate(callerSpeed)
    }

    /**
     * Synthesizes crystal-clear dual tone chime (880Hz -> 1175Hz).
     */
    fun playBallChime() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val durationSec = 0.28
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                val freq1 = 880.0
                val freq2 = 1175.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - (t / durationSec)).coerceIn(0.0, 1.0)
                    val tone1 = sin(2.0 * Math.PI * freq1 * t) * 0.4
                    val tone2 = if (t > 0.1) sin(2.0 * Math.PI * freq2 * (t - 0.1)) * 0.5 else 0.0
                    val mixed = (tone1 + tone2) * env
                    samples[i] = (mixed * Short.MAX_VALUE).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Announces ball call strictly using a pure Amharic male voice.
     * Never falls back to English.
     */
    fun speakBallCall(ballNumber: Int, soundEnabled: Boolean) {
        if (!soundEnabled) return

        // Play chime & gentle tactile buzz
        playBallChime()
        vibrate(45L)

        val amharicCalloutText = BingoEngine.amharicCallout(ballNumber)

        CoroutineScope(Dispatchers.IO).launch {
            // First attempt: High-fidelity natural Amharic audio stream/cache
            val playedAudio = playOnlineAmharicMaleVoice(amharicCalloutText, ballNumber)
            if (!playedAudio) {
                // Secondary fallback: Device Amharic TTS engine in tuned male pitch
                withContext(Dispatchers.Main) {
                    playDeviceAmharicVoice(amharicCalloutText, "ball_$ballNumber")
                }
            }
        }
    }

    /**
     * Plays high-definition Amharic audio tuned with male playback attributes
     */
    private suspend fun playOnlineAmharicMaleVoice(text: String, cacheId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachedFile = File(cacheDir, "call_$cacheId.mp3")
                if (!cachedFile.exists() || cachedFile.length() == 0L) {
                    val encoded = URLEncoder.encode(text, "UTF-8")
                    val urlString = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=am&q=$encoded"
                    val connection = URL(urlString).openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                    connection.connectTimeout = 4000
                    connection.readTimeout = 4000

                    if (connection.responseCode == 200) {
                        connection.inputStream.use { input ->
                            FileOutputStream(cachedFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } else {
                        return@withContext false
                    }
                }

                if (cachedFile.exists() && cachedFile.length() > 0) {
                    withContext(Dispatchers.Main) {
                        try {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                        .setUsage(AudioAttributes.USAGE_GAME)
                                        .build()
                                    )
                                setDataSource(context, Uri.fromFile(cachedFile))
                                // Warm natural male timbre and relaxed, clear pacing
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    playbackParams = playbackParams.apply {
                                        pitch = callerPitch // Warm natural male tone (0.92f avoids distortion)
                                        speed = callerSpeed // Relaxed, stately caller pacing (0.74f, NOT rushed)
                                    }
                                }
                                prepare()
                                start()
                            }
                        } catch (_: Exception) {
                            return@withContext false
                        }
                    }
                    return@withContext true
                }
                false
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun playDeviceAmharicVoice(amharicText: String, utteranceId: String) {
        if (!isTtsReady) return
        try {
            setupAmharicMaleVoice()
            tts?.speak(amharicText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (_: Exception) {
        }
    }

    fun speakBingoWin(winnerName: String) {
        vibrateWin()
        val announcement = "ቢንጎ! $winnerName አሸነፈ!"
        CoroutineScope(Dispatchers.IO).launch {
            val played = playOnlineAmharicMaleVoice(announcement, 9999)
            if (!played) {
                withContext(Dispatchers.Main) {
                    playDeviceAmharicVoice(announcement, "bingo_win")
                }
            }
        }
    }

    fun speakReady(countdownText: String) {
        val amharicReady = when (countdownText.trim()) {
            "3" -> "ሶስት"
            "2" -> "ሁለት"
            "1" -> "አንድ"
            else -> "ተዘጋጁ"
        }
        CoroutineScope(Dispatchers.IO).launch {
            playOnlineAmharicMaleVoice(amharicReady, amharicReady.hashCode())
        }
    }

    fun vibrate(durationMs: Long = 40L) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    fun vibrateWin() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 150, 100, 200, 100, 400)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 150, 100, 200, 100, 400), -1)
            }
        } catch (_: Exception) {
        }
    }

    fun destroy() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }

    fun setCallerSpeed(speed: Float) {
        callerSpeed = speed.coerceIn(0.70f, 1.50f)
        try {
            tts?.setSpeechRate(callerSpeed)
        } catch (_: Exception) {
        }
    }

    fun setCallerPitch(pitch: Float) {
        callerPitch = pitch.coerceIn(0.75f, 1.25f)
        try {
            tts?.setPitch(callerPitch)
        } catch (_: Exception) {
        }
    }

    fun getCallerSpeed(): Float = callerSpeed
    fun getCallerPitch(): Float = callerPitch

    fun testCallerVoice() {
        speakBallCall(15, true)
    }
}
