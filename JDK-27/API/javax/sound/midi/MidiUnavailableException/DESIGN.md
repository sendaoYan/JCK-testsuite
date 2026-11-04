# javax.sound.midi.MidiUnavailableException — DESIGN

## 1. API 功能
`MidiUnavailableException` 继承 `java.lang.Exception`，表示 MIDI 设备/资源当前不可用（如打开失败、无可用设备）。构造器：`MidiUnavailableException()`、`MidiUnavailableException(String)`。常见抛出：`MidiSystem.getReceiver/getTransmitter/getSynthesizer/getSequencer` 及 `MidiDevice.open()`。

## 2. Java 用户如何使用
用户在获取 MIDI 资源时捕获该异常：`try { Sequencer s = MidiSystem.getSequencer(); } catch (MidiUnavailableException ex) { ... }`。典型组合链：`MidiSystem.getReceiver()` 抛 `MidiUnavailableException` → `catch` 读取 `getMessage()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getReceiver()/getTransmitter()/getSynthesizer()/getSequencer()` 均声明 `throws MidiUnavailableException`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiDevice.java`：`open()` 声明 `throws MidiUnavailableException`。
- `test/jdk/javax/sound/midi/Devices/OpenClose.java`、`Devices/MidiIO.java`：真实设备打开/关闭并捕获 `MidiUnavailableException`。
- 该类是标准受检异常，无额外行为，黑盒重点是构造器与 throw/catch 往返。

## 4. 测试用例设计 + 自动化实现（`MidiUnavailableExceptionTest.testBlackBox`）
- 白盒：断言继承 `Exception`、非 abstract、两个构造器、`serialVersionUID` 存在。
- 黑盒：无参构造 `getMessage()==null`、String 构造 `getMessage()` 往返、`throw/catch` 精确类型往返。
