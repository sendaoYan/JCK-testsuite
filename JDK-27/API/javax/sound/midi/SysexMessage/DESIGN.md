# javax.sound.midi.SysexMessage — DESIGN

## 1. API 功能
`SysexMessage` 继承 `MidiMessage`，表示系统专用（system exclusive）消息。方法分组：
- 常量：`SYSTEM_EXCLUSIVE = 0xF0`、`SPECIAL_SYSTEM_EXCLUSIVE = 0xF7`。
- 构造器：`SysexMessage()`、`SysexMessage(byte[] data, int length)`（首字节为状态字节）、`SysexMessage(int status, byte[] data, int length)`（data 不含状态字节）、`protected SysexMessage(byte[])`。
- 设置：`setMessage(byte[], int)`、`setMessage(int, byte[], int)`。
- 查询：`getData()`（返回不含状态字节的拷贝）、继承的 `getStatus/getLength/getMessage`、`clone()`。

## 2. Java 用户如何使用
用户 `new SysexMessage()` 后 `setMessage(SysexMessage.SYSTEM_EXCLUSIVE, data, len)` 构造系统专用消息，或 `new SysexMessage(byte[] data, len)`（data 含 0xF0/0xF7 状态字节）；`getData()` 取回数据。典型组合链：`new SysexMessage()` → `setMessage(0xF0, data, len)` → `getStatus()/getLength()/getData()` → `clone()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/sound/midi/SysexMessage/Basic.java`：覆盖两类状态、两种构造器与两种 `setMessage` 的 status/length/data 往返。
- `test/jdk/javax/sound/midi/SysexMessage/Exceptions.java`：断言缺失状态字节/非法状态抛 `InvalidMidiDataException`、长度越界抛 `IndexOutOfBoundsException`、null 数据抛 `NullPointerException`。
- `src/java.desktop/share/classes/javax/sound/midi/SysexMessage.java`：`setMessage(byte[],int)` 经 `MidiUtils.checkSysexStatus` 校验状态，再调用 `MidiMessage.setMessage`；`setMessage(int,byte[],int)` 校验状态并处理长度。
- `test/jdk/javax/sound/midi/SysexMessage/SendRawSysexMessage.java`、`EmptySysExMessageTest.java`：系统专用消息传输与空消息场景。

## 4. 测试用例设计 + 自动化实现（`SysexMessageTest.testBlackBox`）
- 默认消息：状态 `0xF0`、长度 2、data 为单个 EOX `0xF7`。
- 两种构造器/两种 `setMessage` 往返：status/length/data 逐字节断言，含 `SPECIAL_SYSTEM_EXCLUSIVE`。
- `getData()` 防御性拷贝、`clone()` 独立且内容一致。
- 异常：空 data/长度 0/非法状态抛 `InvalidMidiDataException`；长度越界抛 `IndexOutOfBoundsException`；null 数据抛 `NullPointerException`。
