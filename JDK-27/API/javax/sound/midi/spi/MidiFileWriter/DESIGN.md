# javax.sound.midi.spi.MidiFileWriter — DESIGN

## 1. API 功能
`MidiFileWriter` 是 MIDI 文件写出器的 SPI 抽象基类。方法分组：
- 构造：`protected MidiFileWriter()`。
- 类型：抽象 `getMidiFileTypes()`、`getMidiFileTypes(Sequence)`；默认 `isFileTypeSupported(int)`、`isFileTypeSupported(int,Sequence)`（基于类型数组 `anyMatch`）。
- 写出：抽象 `write(Sequence,int,OutputStream)`、`write(Sequence,int,File)`（抛 `IOException`）。

## 2. Java 用户如何使用
SPI 实现者子类化并注册，`MidiSystem.write`/`getMidiFileTypes` 委托给已安装 writer；用户直接使用 `MidiSystem`。
测试时子类化 `class W extends MidiFileWriter { ... }`，用继承的默认 `isFileTypeSupported` 验证类型匹配。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/spi/MidiFileWriter.java`：`isFileTypeSupported(int)` 默认 `Arrays.stream(getMidiFileTypes()).anyMatch(type -> fileType == type)`；`isFileTypeSupported(int,Sequence)` 基于 `getMidiFileTypes(sequence)`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`write` 对不支持类型抛 `IllegalArgumentException`。

## 4. 测试用例设计 + 自动化实现（`MidiFileWriterTest.testBlackBox`）
- 子类 `FakeWriter`（`getMidiFileTypes()={0,1}`、`getMidiFileTypes(seq)={0}`），断言默认 `isFileTypeSupported` 对 0/1 为真、对 2 为假；带 Sequence 重载对 0 真、1 假。
- `write` 两个重载（OutputStream / 临时目录 File）返回 42，临时目录 finally 清理。
- 白盒补齐：非接口抽象类、extends Object、`protected` 构造、`getMidiFileTypes/isFileTypeSupported/write` 方法齐全。

## 6. 版本差异（JDK-8 vs JDK-27，双侧各自断言）
- 反射 `getDeclaredMethods` 时 JDK-27 的实现类包含 lambda 合成方法——测试须排序/按名匹配而非按序断言。
- 手写 34 字节 SMF（format 0/1）write→read round-trip 两版逐字节一致。
