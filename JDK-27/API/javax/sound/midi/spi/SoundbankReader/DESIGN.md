# javax.sound.midi.spi.SoundbankReader — DESIGN

## 1. API 功能
`SoundbankReader` 是音色库读取器的 SPI 抽象基类。方法分组：
- 构造：`protected SoundbankReader()`。
- 抽象：`getSoundbank(URL/InputStream/File)`（抛 `InvalidMidiDataException`/`IOException`）。

## 2. Java 用户如何使用
SPI 实现者子类化并注册，`MidiSystem.getSoundbank` 遍历已安装 reader 直到成功；用户直接使用 `MidiSystem`。
测试时子类化：`class R extends SoundbankReader { ... }` 实现三个 `getSoundbank` 重载做往返。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/spi/SoundbankReader.java`：三个抽象 `getSoundbank` 方法（`URL/InputStream/File`）。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getSoundbank(stream)` 遍历 `getSoundbankReaders()`，全失败抛 `InvalidMidiDataException("cannot get soundbank from stream")`。

## 4. 测试用例设计 + 自动化实现（`SoundbankReaderTest.testBlackBox`）
- 子类 `FakeSoundbankReader` 缓存固定 `Soundbank`，断言 `getSoundbank` 三种重载（InputStream/URL/File）均返回同一实例。
- 白盒补齐：非接口抽象类、extends Object、`protected` 构造、`getSoundbank` 方法齐全。
