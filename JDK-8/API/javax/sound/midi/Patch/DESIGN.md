# javax.sound.midi.Patch — DESIGN

## 1. API 功能
`Patch` 是不可变的值对象，描述音色（instrument）在 soundbank 中的位置：bank 号（0..16383）与 program 号（0..127）。方法：`Patch(int bank, int program)`、`getBank()`、`getProgram()`。构造器不做校验，原样存储。

## 2. Java 用户如何使用
用户从 `Instrument.getPatch()` 获得 `Patch`，或直接 `new Patch(bank, program)` 表示期望音色。典型组合链：`new Patch(0, 5)` → `getBank()`/`getProgram()` → 传给 `MidiChannel.programChange(bank, program)`。常用作 `Synthesizer.loadInstrument(Instrument)` 等场景的音色定位。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Patch.java`：`private final int bank; private final int program;` 构造器直接赋值。
- `src/java.desktop/share/classes/javax/sound/midi/Instrument.java`：`getPatch()` 返回 `Patch`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiChannel.java`：`programChange(int bank, int program)` 的 bank 即 `Patch.getBank()`。
- `src/java.desktop/share/classes/com/sun/media/sound/ModelPatch.java`（Gervill 内部）：`new Patch(bank, program)` 表示软件合成器音色。

## 4. 测试用例设计 + 自动化实现（`PatchTest.testBlackBox`）
- 白盒：断言非 abstract、继承 `Object`、`bank`/`program` 字段 `final`、方法 `getBank/getProgram`。
- 黑盒：构造器往返；边界值 0 与最大值（16383/127）；构造器不校验（`new Patch(-1,200)` 原样返回）；两个等值 Patch 是独立实例。
