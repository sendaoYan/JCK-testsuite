# javax.sound.midi.MidiDevice.Info — DESIGN

## 1. API 功能
`MidiDevice.Info` 是 `MidiDevice` 的嵌套类，描述 MIDI 设备的四元信息。方法分组：
- 构造：`protected Info(String name, String vendor, String description, String version)`。
- 查询（全部 `final`）：`getName()`、`getVendor()`、`getDescription()`、`getVersion()`、`toString()`（返回 name）。
- 相等性（全部 `final`）：`equals(Object)`、`hashCode()`（基于身份 `Object.equals/hashCode`）。

## 2. Java 用户如何使用
设备提供商继承 `Info` 暴露设备描述，用户通过 `MidiSystem.getMidiDeviceInfo()` 取得数组并读取：
`MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo()` → `infos[0].getName()` → `infos[0].getVendor()` → `infos[0].getDescription()` → `infos[0].getVersion()`。
`MidiSystem.getMidiDevice(info)` 依赖 `Info.equals`（身份相等）匹配已安装设备。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiDevice.java`（`Info` 内部类）：`equals/hashCode` 直接 `super.equals/super.hashCode`（身份语义），`toString()` 返回 `name`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getMidiDeviceInfo()` 把各 provider 的 `Info[]` 追加进 `List`，`getMidiDevice(info)` 以 `provider.isDeviceSupported(info)`（即 `getDeviceInfo()` 中任一 `info::equals`）匹配。
- `test/jdk/javax/sound/midi/MidiSystem/DefaultDevices.java`：遍历 `getMidiDeviceInfo()` 并用 `info` 获取设备。

## 4. 测试用例设计 + 自动化实现（`InfoTest.testBlackBox`）
- 子类 `InfoImpl` 构造，断言 `getName/getVendor/getDescription/getVersion` 往返、`toString()` 返回 name。
- 断言身份相等语义：`equals` 自反真、同字段不同实例 false、`equals(null)`/`equals(String)` false、`hashCode()` 稳定且等于 `System.identityHashCode`。
- 白盒补齐：非接口非抽象、`protected` 构造、7 个 `final` 方法。
