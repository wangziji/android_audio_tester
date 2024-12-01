import numpy as np
import wave
import os

def generate_wav(filename, sample_rate, bit_depth, duration):
    """
    生成指定采样率、位深和持续时间的WAV测试音频文件。

    参数：
        filename (str): 输出文件名。
        sample_rate (int): 采样率，单位为Hz，例如44100, 48000等。
        bit_depth (int): 位深度，支持8, 16, 24和32。
        duration (float): 持续时间，单位为秒。
    """
    if bit_depth == 8:
        dtype = np.uint8
        amplitude = 127
        sampwidth = 1
    elif bit_depth == 16:
        dtype = np.int16
        amplitude = 32767
        sampwidth = 2
    elif bit_depth == 24:
        dtype = np.int32
        amplitude = 8388607
        sampwidth = 3
    elif bit_depth == 32:
        dtype = np.int32
        amplitude = 2147483647
        sampwidth = 4
    else:
        raise ValueError("Unsupported bit depth. Choose from 8, 16, 24, 32.")

    t = np.linspace(0, duration, int(sample_rate * duration), endpoint=False)
    frequency = 440.0  # A4 音符，频率为440Hz
    audio_data = (amplitude * np.sin(2 * np.pi * frequency * t)).astype(dtype)

    # 生成双声道数据，右声道比左声道延迟一个较小的偏移量
    stereo_data = np.zeros((len(audio_data), 2), dtype=dtype)
    stereo_data[:, 0] = audio_data  # 左声道
    stereo_data[:, 1] = audio_data  # 右声道

    # 将数据写入WAV文件
    with wave.open(filename, 'w') as wf:
        wf.setnchannels(2)  # 双声道
        wf.setsampwidth(sampwidth)
        wf.setframerate(sample_rate)
        wf.writeframes(stereo_data.tobytes())

    print(f"生成WAV文件: {filename}, 采样率: {sample_rate} Hz, 位深: {bit_depth} 位, 持续时间: {duration} 秒")

def main():
    output_dir = "wav_test_files"
    os.makedirs(output_dir, exist_ok=True)

    sample_rates = [8000, 16000, 44100, 48000]
    bit_depths = [8, 16, 24, 32]
    duration = 5  # 持续时间为5秒

    for sample_rate in sample_rates:
        for bit_depth in bit_depths:
            filename = os.path.join(output_dir, f"test_{sample_rate}Hz_{bit_depth}bit.wav")
            generate_wav(filename, sample_rate, bit_depth, duration)

if __name__ == "__main__":
    main()
