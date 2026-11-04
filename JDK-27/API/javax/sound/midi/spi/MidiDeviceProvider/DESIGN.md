# javax.sound.midi.spi.MidiDeviceProvider — DESIGN

## 1. API 功能
`MidiDeviceProvider` 是 MIDI 设备提供者的 SPI 抽象基类。方法分组：
- 构造：`protected MidiDeviceProvider()`。
- 默认方法：`isDeviceSupported(MidiDevice.Info)`（默认实现：`getDeviceInfo()` 中任一 `info::equals`）。
- 抽象：`getDeviceInfo()`、`getDevice(MidiDevice.Info)`。

## 2. Java 用户如何使用
SPI 实现者子类化并注册（`META-INF/services`），`MidiSystem` 通过 `ServiceLoader` 发现；用户不直接调用，而是走 `MidiSystem.getMidiDeviceInfo()/getMidiDevice(info)`。
测试时子类化：`class P extends MidiDeviceProvider { public Info[] getDeviceInfo(){...} public MidiDevice getDevice(Info i){...} }`，再调用继承的 `isDeviceSupported`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/spi/MidiDeviceProvider.java`：`isDeviceSupported(info)` 默认实现为 `Arrays.stream(getDeviceInfo()).anyMatch(info::equals)`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getMidiDevice(info)` 先 `Objects.requireNonNull(info)`，再按 `provider.isDeviceSupported(info)` 匹配后 `provider.getDevice(info)`。

## 4. 测试用例设计 + 自动化实现（`MidiDeviceProviderTest.testBlackBox`）
- 子类 `FakeProvider` 提供两个 `Info`，断言 `getDeviceInfo()` 非空/长度/实例、`isDeviceSupported` 对已知 info 为 true、对未知 info 为 false、`getDevice(a)` 非空且确定性（缓存同一设备实例）。
- 白盒补齐：非接口抽象类、extends Object、`protected` 构造、`isDeviceSupported/getDeviceInfo/getDevice` 三方法齐全。
