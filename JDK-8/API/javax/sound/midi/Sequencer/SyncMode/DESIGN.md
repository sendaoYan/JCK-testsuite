# javax.sound.midi.Sequencer.SyncMode — DESIGN

## 1. API 功能
`Sequencer.SyncMode` 是 `Sequencer` 的嵌套类，表示主/从同步模式。方法分组：
- 构造：`protected SyncMode(String name)`。
- 查询（`final`）：`equals(Object)`、`hashCode()`（身份语义）、`toString()`（返回 name）。
- 内置常量：`INTERNAL_CLOCK`（"Internal Clock"）、`MIDI_SYNC`（"MIDI Sync"）、`MIDI_TIME_CODE`（"MIDI Time Code"）、`NO_SYNC`（"No Timing"）。

## 2. Java 用户如何使用
通常使用内置常量：`Sequencer.SyncMode m = Sequencer.SyncMode.INTERNAL_CLOCK` → `sequencer.setMasterSyncMode(m)` → `sequencer.getMasterSyncMode()`。
自定义模式用于 SPI 设备：`class MyMode extends Sequencer.SyncMode { MyMode(String n){ super(n);} }`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Sequencer.java`（`SyncMode` 内部类）：`equals/hashCode` 为 `super`（身份），`toString()` 返回 `name`；四个常量分别 `new SyncMode("Internal Clock"/"MIDI Sync"/"MIDI Time Code"/"No Timing")`。
- `test/jdk/javax/sound/midi/Sequencer/ToString.java`：匿名子类 `new Sequencer.SyncMode("nameToTest"){}`，断言 `toString()` 返回传入名称。

## 4. 测试用例设计 + 自动化实现（`SyncModeTest.testBlackBox`）
- 子类 `SyncImpl` 构造，断言 `toString()` 返回 name、`equals` 自反、`hashCode()` 稳定、同字段不同实例 `equals` false。
- 四个内置常量：`toString()` 精确等于文档名称、互相 `equals` false、常量是单例（`==` 自身引用）。
- 白盒补齐：非接口非抽象、`protected` 构造、`equals/hashCode/toString` 为 `final`、四个 public static final 字段存在。
