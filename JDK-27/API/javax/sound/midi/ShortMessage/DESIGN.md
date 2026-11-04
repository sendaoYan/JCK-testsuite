# javax.sound.midi.ShortMessage — DESIGN

## 1. API 功能
`ShortMessage` 继承 `MidiMessage`，表示数据字节数为 0/1/2 的短 MIDI 消息（通道语音、通道模式、系统公用、系统实时）。方法分组：
- 常量：系统公用（`MIDI_TIME_CODE`、`SONG_POSITION_POINTER`、`SONG_SELECT`、`TUNE_REQUEST`、`END_OF_EXCLUSIVE`）、系统实时（`TIMING_CLOCK`、`START`、`CONTINUE`、`STOP`、`ACTIVE_SENSING`、`SYSTEM_RESET`）、通道命令（`NOTE_OFF`、`NOTE_ON`、`POLY_PRESSURE`、`CONTROL_CHANGE`、`PROGRAM_CHANGE`、`CHANNEL_PRESSURE`、`PITCH_BEND`）。
- 构造器：`ShortMessage()`、`ShortMessage(int status)`、`ShortMessage(int status, int data1, int data2)`、`ShortMessage(int command, int channel, int data1, int data2)`、`protected ShortMessage(byte[])`。
- 设置：`setMessage(int)`、`setMessage(int,int,int)`、`setMessage(int,int,int,int)`。
- 查询：`getChannel()`、`getCommand()`、`getData1()`、`getData2()`、继承的 `getStatus/getLength/getMessage`、`clone()`。

## 2. Java 用户如何使用
用户 `new ShortMessage()` 后 `setMessage(ShortMessage.NOTE_ON, channel, note, velocity)` 构造 Note On，再 `getCommand()/getChannel()/getData1()/getData2()` 读取；或直接 `new ShortMessage(command, channel, data1, data2)`。系统实时消息用 `setMessage(TIMING_CLOCK)`（无数据）。典型组合链：`new ShortMessage()` → `setMessage(NOTE_ON, 0, 60, 100)` → `getStatus/getCommand/getChannel/getData1/getData2` → `clone()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/sound/midi/ShortMessage/FastShortMessage.java`：`ShortMessage msg = new ShortMessage(); msg.setMessage(ShortMessage.NOTE_ON | 9, 0x24, 0x50);` 再写入 Sequence 并逐字节读回。
- `src/java.desktop/share/classes/javax/sound/midi/ShortMessage.java`：`setMessage` 通过 `getDataLength(status)` 判定数据字节数并校验 data1/data2 范围；`setMessage(command, channel, ...)` 校验 command 0x80..0xEF、channel 0..15。
- `test/jdk/javax/sound/midi/Gervill/SoftReceiver/Send_NoteOn.java` 等大量 `SoftReceiver` 用例构造 `ShortMessage` 并发送。
- `src/java.desktop/share/classes/javax/sound/midi/ControllerEventListener.java`：`controlChange(ShortMessage)` 的参数类型即 `ShortMessage`。

## 4. 测试用例设计 + 自动化实现（`ShortMessageTest.testBlackBox`）
- 默认构造：NOTE_ON、channel 0、note 64、velocity 127、length 3。
- 通道消息往返：`setMessage(NOTE_ON,5,60,100)` 的 status/command/channel/data1/data2/getMessage 逐字节断言。
- 单数据消息（Program Change）：data2 忽略、length 2。
- 无数据系统实时消息：`setMessage(TIMING_CLOCK)`、length 1、data 为 0。
- 系统公用两数据消息：Song Position Pointer。
- 4 参构造器与 clone 往返。
- 异常：非法 status、data1/data2 越界、对需要数据的 status 调 `setMessage(int)`、command 越界、channel 越界均抛 `InvalidMidiDataException`。
