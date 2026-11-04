# javax.sound.midi.Instrument — DESIGN

## 1. API 功能
`Instrument` 是 `SoundbankResource` 的抽象子类，代表一个可从 `Soundbank` 加载到 `Synthesizer` 的乐器（补丁）。方法分组：
- 构造：`protected Instrument(Soundbank, Patch, String, Class<?>)`（子类通过 `super(...)` 调用）。
- 查询：`getPatch()` 返回该乐器对应的 `Patch`（bank + program）。
- 继承自 `SoundbankResource`：`getSoundbank()`、`getName()`、`getDataClass()`、抽象 `getData()`。

## 2. Java 用户如何使用
用户不能直接 `new Instrument`，而是通过 `Soundbank.getInstruments()` 或 `Synthesizer.getAvailableInstruments()` 获得实例，再调用：
`Instrument i = synth.getAvailableInstruments()[0]` → `i.getPatch()` → `i.getName()` → `i.getSoundbank()`。
子类测试时：`class MyInst extends Instrument { MyInst(Soundbank sb, Patch p, String n, Class<?> c){ super(sb,p,n,c);} public Object getData(){ return ...; } }`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Instrument.java`：字段 `private final Patch patch`，`getPatch()` 直接返回构造时传入的 `patch`。
- `src/java.desktop/share/classes/javax/sound/midi/SoundbankResource.java`：`Instrument` 继承其 `soundBank/name/dataClass` 三个 final 字段，`getSoundbank()/getName()/getDataClass()` 均返回构造参数。
- `test/jdk/javax/sound/midi/Synthesizer` 系列用例通过 `getDefaultSoundbank().getInstruments()` 取得 `Instrument`，再用 `loadInstrument(instrument)` 加载（本类与 `Synthesizer` 组合使用）。

## 4. 测试用例设计 + 自动化实现（`InstrumentTest.testBlackBox`）
- 用 `FakeSB implements Soundbank` + `new Patch(3,42)` + 子类 `Inst` 构造实例。
- 断言 `getPatch()` 返回同一 `Patch` 实例，且 `getBank()/getProgram()` 往返一致。
- 断言继承链：`getName()`、`getDataClass()`、`getSoundbank()` 与构造参数一致；`getData()` 返回子类实现的载荷对象。
- 白盒补齐：抽象类、extends `SoundbankResource`、无接口、声明 `getPatch()`。
