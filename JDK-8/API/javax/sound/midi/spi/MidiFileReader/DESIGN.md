# javax.sound.midi.spi.MidiFileReader — DESIGN

## 1. API 功能
`MidiFileReader` 是 MIDI 文件读取器的 SPI 抽象基类。方法分组：
- 构造：`protected MidiFileReader()`。
- 抽象：`getMidiFileFormat(InputStream/URL/File)`（抛 `InvalidMidiDataException`/`IOException`）、`getSequence(InputStream/URL/File)`（同异常）。

## 2. Java 用户如何使用
SPI 实现者子类化并注册，`MidiSystem.getSequence/getMidiFileFormat` 遍历所有已安装 reader 直到某 reader 成功；用户直接使用 `MidiSystem`，不直接调用本类。
测试时子类化：`class R extends MidiFileReader { ... }` 实现六个抽象方法做往返。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/spi/MidiFileReader.java`：六个抽象方法（`getMidiFileFormat`/`getSequence` × `InputStream/URL/File`）。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getSequence(stream)` 遍历 `getMidiFileReaders()` 逐个 `reader.getSequence(stream)`，`InvalidMidiDataException` 则 continue，全失败抛 `InvalidMidiDataException`。

## 4. 测试用例设计 + 自动化实现（`MidiFileReaderTest.testBlackBox`）
- 子类 `FakeReader` 缓存固定 `MidiFileFormat` 与 `Sequence`，断言 `getMidiFileFormat/getSequence` 三种重载（InputStream/URL/File）均返回缓存的同一实例。
- 白盒补齐：非接口抽象类、extends Object、`protected` 构造、`getMidiFileFormat/getSequence` 方法齐全。
