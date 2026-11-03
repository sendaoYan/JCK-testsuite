# javax.sound.midi.MidiChannel — DESIGN

## 1. API 功能
`MidiChannel` 是代表 16 个 MIDI 通道之一的接口（无直接父接口），描述向合成器某通道发送音符/控制消息及读取通道状态。方法分组：
- 音符：`noteOn(int note, int velocity)`、`noteOff(int note)`、`noteOff(int note, int velocity)`。
- 触后/压力：`setPolyPressure(int,int)`、`getPolyPressure(int)`、`setChannelPressure(int)`、`getChannelPressure()`。
- 控制：`controlChange(int,int)`、`getController(int)`、`programChange(int)`、`programChange(int,int)`、`getProgram()`、`setPitchBend(int)`、`getPitchBend()`。
- 状态：`resetAllControllers()`、`allNotesOff()`、`allSoundOff()`、`localControl(boolean)`、`setMono(boolean)`/`getMono()`、`setOmni(boolean)`/`getOmni()`、`setMute(boolean)`/`getMute()`、`setSolo(boolean)`/`getSolo()`。

## 2. Java 用户如何使用
用户通过 `MidiSystem.getSynthesizer().getChannels()[ch]` 获得通道，发送 `noteOn/noteOff`，并用 `programChange/controlChange/setPitchBend` 控制音色与表现。典型组合链：`Synthesizer syn = MidiSystem.getSynthesizer(); MidiChannel ch = syn.getChannels()[0]; ch.programChange(0, 5); ch.noteOn(60, 100); ch.noteOff(60); ch.setPitchBend(8192);`。设置方法是异步命令，getter 返回设备侧最近值。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/sound/midi/Gervill/SoftChannel/*.java` 与 `SoftReceiver/Send_*.java`：大量通过 `SoftChannel` 实例调用 `noteOn/noteOff/controlChange/programChange/setPitchBend` 并断言 getter 返回。
- `test/jdk/javax/sound/midi/Synthesizer/AsynchronousMidiChannel.java`：验证 `MidiChannel` 设置/读取是异步的（getter 不保证同步反映 setter）。
- `src/java.desktop/share/classes/javax/sound/midi/Synthesizer.java`：`getChannels()` 返回 `MidiChannel[]`。
- `src/java.desktop/share/classes/com/sun/media/sound/SoftChannel.java`：`MidiChannel` 的软件合成器实现（`SoftChannelProxy` 代理）。

## 4. 测试用例设计 + 自动化实现（`MidiChannelTest.testBlackBox`）
- 白盒：断言 isInterface、无直接父接口、26 个抽象方法及名称齐全。
- 黑盒：通过 `MidiSystem.getSynthesizer().getChannels()[0]` 获得真实通道（无合成器时降级为仅白盒）。断言命令方法 `noteOn/noteOff/controlChange/programChange/setPitchBend/setPolyPressure/setChannelPressure/resetAllControllers/allNotesOff/allSoundOff` 不抛异常；各 getter 返回值落在 MIDI 合法范围（0..127 或 0..16383、boolean）。设置与读取采用异步语义，不依赖具体初值，保证 headless/多设备确定。
