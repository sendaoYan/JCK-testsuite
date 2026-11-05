# javax.sound.sampled.AudioSystem — DESIGN

## 1. API 功能
`AudioSystem` 是 Java Sound 采样音频的静态入口，无实例、不可继承调用（构造器私有）。方法分组：
- 文件类型：`getAudioFileTypes()`、`getAudioFileTypes(AudioInputStream)`、`isFileTypeSupported(Type)`、`isFileTypeSupported(Type, AudioInputStream)`。
- 文件读写：`write(AudioInputStream, Type, OutputStream/File)`、`getAudioFileFormat(InputStream/URL/File)`、`getAudioInputStream(InputStream/URL/File)`。
- 混音器：`getMixerInfo()`、`getMixer(Mixer.Info)`。
- 线路：`getSourceLineInfo(Line.Info)`、`getTargetLineInfo(Line.Info)`、`isLineSupported(Line.Info)`、`getLine(Line.Info)`、`getClip()`、`getSourceDataLine(AudioFormat)`、`getTargetDataLine(AudioFormat)`。
- 格式转换：`getTargetEncodings(...)`、`isConversionSupported(...)`、`getTargetFormats(...)`、`getAudioInputStream(Encoding/AudioFormat, AudioInputStream)`。
- 常量：`NOT_SPECIFIED = -1`。

## 2. Java 用户如何使用
典型组合链：`AudioSystem.getAudioFileTypes()` → `AudioSystem.write(ais, Type.WAVE, out)` 写入内存字节 → `AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav))` 读回并断言帧长 → `AudioSystem.getAudioFileFormat(...)` 读元数据；`AudioSystem.getMixerInfo()` → `AudioSystem.getMixer(info)` → `mixer.getSourceLineInfo()`；`AudioSystem.getSourceDataLine(fmt)` → `open/start/write/stop/close`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/sound/sampled/Mixers/UnexpectedIAE.java`：`Mixer.Info[] infos = AudioSystem.getMixerInfo(); Mixer mixer = AudioSystem.getMixer(infos[i]); mixer.getTargetLineInfo()`。
- `test/jdk/javax/sound/sampled/Lines/GetLine.java`：`DataLine.Info s_info = new DataLine.Info(SourceDataLine.class, null); AudioSystem.getSourceLineInfo(s_info); AudioSystem.getLine(infos[0])`。
- `test/jdk/javax/sound/sampled/Lines/SDLwrite.java`：`AudioSystem.getMixerInfo()` 空则 PASS，否则 `AudioSystem.getMixer(...)` 后测试 SourceDataLine.write。
- `test/jdk/javax/sound/sampled/FileTypeExtension/FileTypeExtensionTest.java`：`AudioFileFormat.Type.WAVE.getExtension()` 不含 `.`。

## 4. 测试用例设计 + 自动化实现（`AudioSystemTest.testBlackBox`）
- 白盒：公开类、非抽象、`NOT_SPECIFIED` 静态字段值为 -1。
- 黑盒：内存 WAV 往返（write→getAudioInputStream/getAudioFileFormat 帧长 800、声道/采样率/位深）；`getAudioFileTypes` 含 WAVE、`isFileTypeSupported`；bogus 输入抛 `UnsupportedAudioFileException`；`getMixerInfo` 非空；`getMixer(bogus)` 抛 `IllegalArgumentException`；`getMixer(null)` 兼容返回 mixer 或抛 IAE；`isLineSupported` 与 `getSourceLineInfo` 一致性；存在 mixer 时查询线路并 open/close SourceDataLine（无线路时跳过）。

## 6. 版本差异（JDK-8 vs JDK-27，双侧各自断言）
- AU 头 size 字段 < 24（offset）：JDK-8 宽容接受并按 `byteLength=offset+sizeField` 计算（size=3→27、0→24、-1→23）；JDK-27 抛 `UnsupportedAudioFileException("Stream of unsupported format")`。
- AU size=0x7FFFFFFF：JDK-8 byteLength 溢出为 -2147483625，JDK-27 归一为 -1。
- WAV fmt tag=99：JDK-8 消息 `file is not a supported file type`，JDK-27 `Stream of unsupported format`。
- 1 字节输入：JDK-8 抛 `EOFException`，JDK-27 抛 `UnsupportedAudioFileException`。
- `getTargetEncodings`：8-bit 源 JDK-8 返回 5 个（含重复），JDK-27 返回 4 个且顺序不同——断言集合成员关系而非顺序。
- 不支持转换的 IAE 消息：JDK-8 以 `, ` 结尾。
