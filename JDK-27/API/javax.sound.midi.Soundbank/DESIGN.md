# javax.sound.midi.Soundbank — DESIGN

## 1. API 功能
`Soundbank` 是音色库接口。方法分组：
- 元信息：`getName()`、`getVersion()`、`getVendor()`、`getDescription()`。
- 资源/乐器：`getResources()`、`getInstruments()`、`getInstrument(Patch)`。

## 2. Java 用户如何使用
通过 `Synthesizer.getDefaultSoundbank()` 获得实例：`Soundbank sb = synth.getDefaultSoundbank()` → `sb.getName()` → `sb.getVendor()` → `sb.getInstruments()` → `sb.getInstrument(patch)` → `synth.loadAllInstruments(sb)`。
接口实现者需返回非空数组；`getInstrument(Patch)` 按 bank+program 匹配返回乐器，未命中返回 `null`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Soundbank.java`：接口 7 个方法定义。
- `src/java.desktop/share/classes/javax/sound/midi/Synthesizer.java`：`getDefaultSoundbank()`、`isSoundbankSupported(Soundbank)`、`loadAllInstruments(Soundbank)` 等均以 `Soundbank` 为参数。
- `test/jdk/javax/sound/midi/Soundbanks/EmptySoundBankTest.java`：构造空音色库验证 `getInstruments()/getResources()` 行为。

## 4. 测试用例设计 + 自动化实现（`SoundbankTest.testBlackBox`）
- 用 `FakeSB implements Soundbank`（内含 1 个 `FakeInstrument` 与 1 个 `FakeResource`）走接口契约。
- 断言 `getName/getVersion/getVendor/getDescription` 往返、`getResources()/getInstruments()` 长度、`getInstrument(inst.getPatch())` 返回同一乐器、`getInstrument(new Patch(99,99))` 返回 null。
- 白盒补齐：接口、无父接口、7 个方法齐全。
