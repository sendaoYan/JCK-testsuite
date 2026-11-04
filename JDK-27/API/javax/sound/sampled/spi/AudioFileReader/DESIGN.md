# javax.sound.sampled.spi.AudioFileReader — DESIGN

## 1. API 功能
`AudioFileReader` 是音频文件读取服务的 SPI 抽象基类。方法（全抽象，供 provider 实现）：
- `getAudioFileFormat(InputStream/URL/File)`。
- `getAudioInputStream(InputStream/URL/File)`。
构造器 `protected`；具体 provider 通过 `AudioSystem.getAudioFileFormat/getAudioInputStream` 被消费者间接调用。

## 2. Java 用户如何使用
最终用户不直接实例化它，而是实现子类并通过 `META-INF/services/javax.sound.sampled.spi.AudioFileReader` 注册；或直接调用 `AudioSystem.getAudioFileFormat(stream)` 触发已注册 reader。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioSystem.java:902-1040`：`getAudioFileFormat/getAudioInputStream` 内部遍历 `getAudioFileReaders()` 逐个尝试。
- 内置实现 `com.sun.media.sound.WaveFileReader/AiffFileReader/AuFileReader` 实现该 SPI。

## 4. 测试用例设计 + 自动化实现（`AudioFileReaderTest.testBlackBox`）
- 白盒：抽象类、protected 构造器、继承 Object、6 个抽象方法。
- 黑盒：匿名子类可实例化证明 protected 构造器可用；通过 `AudioSystem` 公共 API 走真实 reader：WAV 写入内存后 `getAudioFileFormat` 类型 WAVE、`getAudioInputStream` 帧长 800；bogus 输入抛 `UnsupportedAudioFileException`。
