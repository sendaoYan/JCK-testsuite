# javax.sound.midi.Synthesizer — DESIGN

## 1. API 功能
`Synthesizer` 是发声设备接口（extends `MidiDevice`）。方法分组：
- 容量/延迟：`getMaxPolyphony()`（-1 表示无限制）、`getLatency()`。
- 通道/声音：`getChannels()`、`getVoiceStatus()`。
- 音色库：`getDefaultSoundbank()`、`isSoundbankSupported(Soundbank)`。
- 乐器加载：`getAvailableInstruments()`、`getLoadedInstruments()`、`loadInstrument(Instrument)`、`unloadInstrument(Instrument)`、`remapInstrument(Instrument,Instrument)`、`loadAllInstruments(Soundbank)`、`unloadAllInstruments(Soundbank)`、`loadInstruments(Soundbank,Patch[])`、`unloadInstruments(Soundbank,Patch[])`。

## 2. Java 用户如何使用
`Synthesizer syn = MidiSystem.getSynthesizer()` → `syn.getDefaultSoundbank()` → `syn.getChannels()` → `syn.getAvailableInstruments()` → `syn.loadInstrument(instrument)` → `syn.getLoadedInstruments()` → `syn.close()`。
无默认合成器时 `getSynthesizer()` 抛 `MidiUnavailableException`，需降级。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Synthesizer.java`：接口 15 个方法定义，extends `MidiDevice`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getSynthesizer()` 返回默认合成器。
- `test/jdk/javax/sound/midi/Synthesizer` 系列用例：`getDefaultSoundbank()` 后 `loadAllInstruments`/`loadInstrument` 验证乐器加载。

## 4. 测试用例设计 + 自动化实现（`SynthesizerTest.testBlackBox`）
- `MidiSystem.getSynthesizer()`（`MidiUnavailableException` 时降级 return）。
- 断言 `getMaxPolyphony()>=-1`、`getLatency()>=0`、`getChannels()` 非空、`getVoiceStatus()`/`getAvailableInstruments()`/`getLoadedInstruments()` 非空。
- 若 `getDefaultSoundbank()` 非空：name/vendor/version/description 非空、`getInstruments()/getResources()` 非空、`isSoundbankSupported(sb)` 为真、`loadAllInstruments(sb)` 无异常、`getLoadedInstruments()` 非空；取首个乐器断言 patch/name 非空、`getSoundbank()==sb`、`loadInstrument/unloadInstrument/remapInstrument` 无异常（用 `noThrow` 包装）。
- finally 中 `close()` 合成器。
