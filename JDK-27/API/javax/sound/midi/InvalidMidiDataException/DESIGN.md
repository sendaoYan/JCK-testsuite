# javax.sound.midi.InvalidMidiDataException — DESIGN

## 1. API 功能
`InvalidMidiDataException` 继承 `java.lang.Exception`，表示遇到非法 MIDI 数据。构造器：`InvalidMidiDataException()`、`InvalidMidiDataException(String)`。常见抛出场景：`ShortMessage`/`MetaMessage`/`SysexMessage` 的 `setMessage` 与构造器收到越界状态字节、非法类型、非法长度等。

## 2. Java 用户如何使用
用户在构造 MIDI 消息时捕获该异常：`try { msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 100); } catch (InvalidMidiDataException ex) { ... }`。典型组合链：构造 `ShortMessage`/`MetaMessage`/`SysexMessage` → `setMessage(...)` 抛 `InvalidMidiDataException` → `catch` 读取 `getMessage()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/ShortMessage.java`：`setMessage` 在 `getDataLength`/数据越界时 `throw new InvalidMidiDataException(...)`。
- `src/java.desktop/share/classes/javax/sound/midi/MetaMessage.java`：`setMessage` 对 `type>=128` 或长度越界抛 `InvalidMidiDataException`。
- `test/jdk/javax/sound/midi/SysexMessage/Exceptions.java`：大量断言 `new SysexMessage(...)`/`setMessage(...)` 抛 `InvalidMidiDataException` 的用例。
- 该类是标准受检异常，无额外行为，黑盒重点是构造器与 throw/catch 往返。

## 4. 测试用例设计 + 自动化实现（`InvalidMidiDataExceptionTest.testBlackBox`）
- 白盒：断言继承 `Exception`、非 abstract、两个构造器、`serialVersionUID` 存在。
- 黑盒：无参构造 `getMessage()==null`、String 构造往返 `getMessage()`、空串消息往返、`throw/catch` 精确类型往返。
