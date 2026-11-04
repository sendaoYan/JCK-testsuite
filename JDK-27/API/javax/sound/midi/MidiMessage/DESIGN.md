# javax.sound.midi.MidiMessage — DESIGN

## 1. API 功能
`MidiMessage` 是所有 MIDI 消息（`ShortMessage`、`SysexMessage`、`MetaMessage`）的抽象基类，实现 `java.lang.Cloneable`。方法分组：
- 受保护构造/设置：`protected MidiMessage(byte[] data)`、`protected setMessage(byte[] data, int length)`。
- 查询：`getMessage()`（返回长度 `getLength()` 的拷贝，首字节为状态字节）、`getStatus()`（无内容时返回 0）、`getLength()`。
- 抽象：`public abstract Object clone()`。

## 2. Java 用户如何使用
用户不直接实例化 `MidiMessage`，而是使用 `ShortMessage`/`SysexMessage`/`MetaMessage` 后调用继承的 `getMessage()/getStatus()/getLength()`。典型组合链：`new ShortMessage(); setMessage(...)` → `getStatus()`/`getLength()` → `getMessage()` 逐字节读取 → `clone()`。子类通过 `protected MidiMessage(byte[])` 与 `protected setMessage(byte[], int)` 复用底层存储。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiMessage.java`：`setMessage` 校验 `length<0 || length>data.length` 抛 `IndexOutOfBoundsException`；`getMessage()`/`getStatus()`/`getLength()` 基于 `data` 与 `length`。
- `src/java.desktop/share/classes/javax/sound/midi/ShortMessage.java`：`public ShortMessage() { this(new byte[3]); ... }` 通过受保护构造器复用。
- `src/java.desktop/share/classes/javax/sound/midi/SysexMessage.java`/`MetaMessage.java`：重写/调用 `setMessage` 与 `clone`。
- `test/jdk/javax/sound/midi/ShortMessage/FastShortMessage.java`：`msg.getMessage()` 逐字节断言读取往返。

## 4. 测试用例设计 + 自动化实现（`MidiMessageTest.testBlackBox`）
- 白盒：断言 abstract、继承 `Object`、实现 `Cloneable`、受保护构造器与 `setMessage`、`getMessage/getStatus/getLength/clone`。
- 黑盒：用包内静态子类 `TestMessage extends MidiMessage` 走 `protected` 构造器与 `setMessage`；断言 `getLength/getStatus/getMessage` 往返；`getMessage()` 防御性拷贝；`clone()` 独立且内容一致；`setMessage` 负长度/超长抛 `IndexOutOfBoundsException`。
