# javax.sound.midi.MetaMessage — DESIGN

## 1. API 功能
`MetaMessage` 继承 `MidiMessage`，表示 MIDI 文件中的元事件（tempo、歌词、版权、拍号、调号、marker 等）。方法分组：
- 构造器：`MetaMessage()`、`MetaMessage(int type, byte[] data, int length)`、`protected MetaMessage(byte[])`。
- 设置：`setMessage(int type, byte[] data, int length)`（type 需 0..127，data 后跟可变长长度字段）。
- 查询：`getType()`、`getData()`（返回拷贝，不含状态字节与长度字段）、继承的 `getStatus/getLength/getMessage`。
- 其它：`clone()`、常量 `META = 0xFF`。

## 2. Java 用户如何使用
用户 `new MetaMessage()` 后 `setMessage(0x51, tempoBytes, 3)` 设置速度事件，再用 `getType()/getData()` 读取；或直接 `new MetaMessage(type, data, length)`。典型组合链：`new MetaMessage()` → `setMessage(type, data, length)` → `getType()`/`getData()`/`getLength()` → `clone()`。`getStatus()` 恒为 `0xFF`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/sound/midi/MetaMessage/MetaMessageClone.java`：`MetaMessage msg = new MetaMessage(); msg.setMessage(1, text.getBytes(), text.length()); MetaMessage msg2 = (MetaMessage) msg.clone();` 并断言 type/data/length 相等，覆盖 0、80、160、400、1000、17000 等长度。
- `src/java.desktop/share/classes/javax/sound/midi/MetaMessage.java`：`setMessage` 校验 `type` 与 `length`，`getData()` 返回 `dataLength` 长度拷贝。
- `test/jdk/javax/sound/midi/Sequencer/MetaCallback.java`：通过 `MetaEventListener` 收到 `MetaMessage` 后读取 `getType()/getData()`。
- 长度编码使用可变长 quantity：<128 用 1 字节，>=128 用 2 字节（源码 `getVarIntLength/writeVarInt`）。

## 4. 测试用例设计 + 自动化实现（`MetaMessageTest.testBlackBox`）
- 默认消息：`getStatus()==0xFF`、`getType()==0`、`getLength()==2`、`getData()` 空。
- 构造器/setMessage 往返：type/status/data/length/getMessage 逐字节断言（含可变长长度字段）。
- 可变长编码：长度 128 → 总长 132、data 长度 128。
- 防御性拷贝：修改 `getData()` 返回数组不影响原消息。
- clone：独立对象、type/length/data 一致。
- 异常：`type>=128`、`type<0`、`length>data.length`、`length<0` 均抛 `InvalidMidiDataException`。
