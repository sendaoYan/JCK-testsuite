# javax.sound.sampled.spi.AudioFileWriter — DESIGN

## 1. API 功能
`AudioFileWriter` 是音频文件写入服务的 SPI 抽象基类。方法：
- 抽象：`getAudioFileTypes()`、`getAudioFileTypes(AudioInputStream)`、`write(AudioInputStream, Type, OutputStream)`、`write(AudioInputStream, Type, File)`。
- 具体：`isFileTypeSupported(Type)`、`isFileTypeSupported(Type, AudioInputStream)`（默认基于 `getAudioFileTypes` 数组判断）。
构造器 `protected`。

## 2. Java 用户如何使用
最终用户通过 `AudioSystem.write(ais, type, out)` 与 `AudioSystem.getAudioFileTypes()` 间接使用已注册 writer；自定义 provider 实现抽象方法并通过 SPI 注册。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioSystem.java:1057-1215`：`getAudioFileTypes` 聚合所有 writer，`write` 逐个尝试 writer 并忽略 `IllegalArgumentException`。
- 内置 `WaveFileWriter/AiffFileWriter/AuFileWriter` 实现该 SPI。

## 4. 测试用例设计 + 自动化实现（`AudioFileWriterTest.testBlackBox`）
- 白盒：抽象类、protected 构造器、4 个抽象方法、2 个具体 `isFileTypeSupported`。
- 黑盒：匿名子类返回空类型数组时 `isFileTypeSupported` 为 false；真实 writer 通过 `AudioSystem.getAudioFileTypes()` 非空、`isFileTypeSupported(WAVE)` 为 true、`write` 写入内存返回正字节数。
