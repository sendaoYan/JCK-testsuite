# javax.sound.midi.MidiSystem — DESIGN

## 1. API 功能
`MidiSystem` 是 MIDI 子系统的静态门面（不可实例化，构造器 private）。方法分组：
- 设备/端口：`getMidiDeviceInfo()`、`getMidiDevice(Info)`、`getReceiver()`、`getTransmitter()`、`getSynthesizer()`、`getSequencer()`/`getSequencer(boolean)`。
- Soundbank 读取：`getSoundbank(InputStream/URL/File)`。
- MIDI 文件读取：`getMidiFileFormat(InputStream/URL/File)`、`getSequence(InputStream/URL/File)`。
- MIDI 文件写出/类型：`getMidiFileTypes()`/`getMidiFileTypes(Sequence)`、`isFileTypeSupported(int)`/`isFileTypeSupported(int,Sequence)`、`write(Sequence,int,OutputStream/File)`。

## 2. Java 用户如何使用
典型链路：`new Sequence(PPQ,96)` → `createTrack()` → `MidiSystem.write(seq, 0, bos)` → `MidiSystem.getSequence(new ByteArrayInputStream(bytes))` → `MidiSystem.getMidiFileFormat(...)`。
设备获取：`MidiSystem.getMidiDeviceInfo()` → `MidiSystem.getMidiDevice(info)`；异常路径：`getMidiDevice(null)` 抛 `NPE`，未知 Info 抛 `IllegalArgumentException`，空流 `getSequence/getMidiFileFormat` 抛 `IOException`（EOFException），空流 `getSoundbank` 抛 `InvalidMidiDataException`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getMidiDevice(info)` 用 `Objects.requireNonNull(info)` 后按 provider 匹配，未命中抛 `IllegalArgumentException`；`getSequence(stream)` 遍历 `MidiFileReader`，全部失败后抛 `InvalidMidiDataException`；`write` 对不支持类型抛 `IllegalArgumentException`。
- `test/jdk/javax/sound/midi/MidiSystem/GetSequencer.java`：`MidiSystem.getSequencer()` 后 `open()/close()`。
- `test/jdk/javax/sound/midi/MidiSystem/DefaultDevices.java`：枚举默认设备与端口，处理 `MidiUnavailableException`。

## 4. 测试用例设计 + 自动化实现（`MidiSystemTest.testBlackBox`）
- 枚举 `getMidiDeviceInfo()` 非空；文件类型非空、`isFileTypeSupported` 两重载。
- `write` 到 OutputStream 后 `getSequence`/`getMidiFileFormat` 往返（分辨率/类型/字节长度）；再 `write` 到临时目录 File 后 `getSequence(File)`/`getMidiFileFormat(File)` 往返（finally 清理）。
- 异常路径：`getMidiDevice(null)` NPE、未知 Info IAE、`getSequence/getMidiFileFormat(null)` NPE、空流 IOException、空流 `getSoundbank` InvalidMidiDataException、不支持类型 `write` IAE。
- 设备降级：`getSequencer/getSynthesizer/getReceiver/getTransmitter` 成功则断言非空并 close，`MidiUnavailableException` 时降级标记。
