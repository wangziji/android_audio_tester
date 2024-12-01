@file:OptIn(ExperimentalTvMaterial3Api::class)

package wang.markz.android.audio.test

import android.content.res.AssetManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wang.markz.android.audio.test.ui.theme.AndroidAudioTestTheme
import java.io.BufferedInputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val tag = "AudioTest"

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 扫描res/raw.wav_test_files目录下的音频文件
        val audioFiles = assets.list("wav_test_files")?.toList()
        for (audioFile in audioFiles!!) {
            Log.i(tag, "onCreate: $audioFile")
        }
        setContent {
            AndroidAudioTestTheme {
                // 左侧创建scrollable list展示文件列表
                FileList(audioFiles)

            }
        }
    }
}

@Composable
fun FileList(files: List<String>?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    files?.let {
        Row(modifier = modifier.fillMaxSize()) {
            //靠左侧
            LazyColumn(
                modifier = modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .wrapContentWidth()
                    .background(color = androidx.compose.ui.graphics.Color.Gray),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)

            ) {
                items(files.size) { index ->
                    Text(
                        files[index].uppercase(),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable {
                                playTestSound(activity?.assets, files[index])
                            },
                    )
                    // 分割线
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(1.dp)
                            .background(color = androidx.compose.ui.graphics.Color.White)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(color = androidx.compose.ui.graphics.Color.Black)
            ) {
                Row(modifier = modifier.fillMaxSize()) {
                    Column(modifier = modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth())
                    {
                        Text("Input", modifier.padding(8.dp),
                            color = androidx.compose.ui.graphics.Color.White)
                        Text("Right2", modifier.padding(8.dp),
                            color = androidx.compose.ui.graphics.Color.White)
                    }

                    Column(modifier = modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth())
                    {
                        Text("Input", modifier.padding(8.dp),
                            color = androidx.compose.ui.graphics.Color.White)
                        Text("Right2", modifier.padding(8.dp),
                            color = androidx.compose.ui.graphics.Color.White)
                    }

                }
            }
        }




    }
}

// Helper function to extract integer values from WAV header
private fun extractInt(data: ByteArray, start: Int): Int {
    return (data[start].toInt() and 0xFF) or
            ((data[start + 1].toInt() and 0xFF) shl 8) or
            ((data[start + 2].toInt() and 0xFF) shl 16) or
            ((data[start + 3].toInt() and 0xFF) shl 24)
}

// Helper function to extract short values from WAV header
private fun extractShort(data: ByteArray, start: Int): Int {
    return (data[start].toInt() and 0xFF) or
            ((data[start + 1].toInt() and 0xFF) shl 8)
}

fun playTestSound(assets: AssetManager?, s: String) {
    if (assets == null) {
        Log.e("AudioTest", "playTestSound: AssetManager is null")
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        // 播放wav文件
        Log.i("AudioTest", "playTestSound: $s")
        try {
            // 打开 assets 目录中的文件
            val inputStream = assets.open("wav_test_files/$s")
            val bufferedInputStream = BufferedInputStream(inputStream)

            val wavHeader = ByteArray(44)
            bufferedInputStream.read(wavHeader, 0, 44)

            // 解析 WAV 文件头信息
            val sampleRate = extractInt(wavHeader, 24)
            val bitDepth = extractShort(wavHeader, 34)
            val channels = extractShort(wavHeader, 22)

            Log.i("AudioTest", "Sample Rate: $sampleRate Hz, Bit Depth: $bitDepth bits, Channels: $channels")

            // 设置 AudioTrack 参数
            val audioFormat = if (bitDepth == 16) {
                AudioFormat.ENCODING_PCM_16BIT
            } else {
                AudioFormat.ENCODING_PCM_8BIT // 其他位深可以根据需要扩展
            }

            val channelConfig = if (channels == 1) {
                AudioFormat.CHANNEL_OUT_MONO
            } else {
                AudioFormat.CHANNEL_OUT_STEREO
            }

            val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            val buffer = ByteArray(bufferSize)
            audioTrack.play()

            // 播放 WAV 文件音频数据
            var bytesRead: Int
            while (bufferedInputStream.read(buffer).also { bytesRead = it } > 0) {
                audioTrack.write(buffer, 0, bytesRead)
            }

            audioTrack.stop()
            audioTrack.release()
            bufferedInputStream.close()
            AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)

            // 打印 AudioTrack 实际输出的参数
            Log.i("AudioTest", "AudioTrack Output Sample Rate: ${AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)} Hz")
            Log.i("AudioTest", "AudioTrack Output Channel Count: ${audioTrack.channelCount}")

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("AudioTest", "playTestSound: Error playing sound", e)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidAudioTestTheme {
        FileList(listOf("test_8000Hz_8bit.wav", "test_16000Hz_16bit.wav"))

    }
}