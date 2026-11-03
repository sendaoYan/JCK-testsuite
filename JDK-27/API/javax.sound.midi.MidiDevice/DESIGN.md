# javax.sound.midi.MidiDevice — DESIGN

## 1. API 功能
`MidiDevice` 是 MIDI 设备的接口（extends `java.lang.AutoCloseable`）。方法分组：
- 元信息：`getDeviceInfo()`。
- 生命周期：`open()`（抛 `MidiUnavailableException`）、`close()`、`isOpen()`。
- 时间/容量查询：`getMicrosecondPosition()`、`getMaxReceivers()`、`getMaxTransmitters()`（-1 表示无限）。
- 端口：`getReceiver()`、`getReceivers()`、`getTransmitter()`、`getTransmitters()`。

## 2. Java 用户如何使用
通过 `MidiSystem.getMidiDeviceInfo()` 枚举设备，再 `MidiSystem.getMidiDevice(info)` 取得实例：
`MidiDevice d = MidiSystem.getMidiDevice(info)` → `d.open()` → `d.getReceiver()` → `d.getMicrosecondPosition()` → `d.close()`。
`getReceiver()`/`getTransmitter()` 可能因设备无对应端口抛 `MidiUnavailableException`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiDevice.java`：接口定义 11 个方法，`open/close/isOpen` 组成标准设备生命周期。
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getMidiDeviceInfo()` 遍历所有 `MidiDeviceProvider` 汇总 Info；`getMidiDevice(info)` 用 `Objects.requireNonNull(info)` 后按 `isDeviceSupported` 匹配。
- `test/jdk/javax/sound/midi/MidiSystem/DefaultDevices.java`：获取默认设备并 `open()/close()` 验证设备生命周期。

## 4. 测试用例设计 + 自动化实现（`MidiDeviceTest.testBlackBox`）
- 遍历 `MidiSystem.getMidiDeviceInfo()`，对每个可打开设备走完整生命周期：`getDeviceInfo()` 非空、初始 `isOpen()==false`、`open()` 后 `isOpen()==true`、`getMicrosecondPosition()>=0`、`getMaxReceivers()/getMaxTransmitters()>=-1`、`getReceivers()/getTransmitters()` 非空、`getReceiver()/getTransmitter()` 成功则非空并 `close()`、最后 `close()` 后 `isOpen()==false`。
- 无设备/无法打开时自然降级（循环体为空，仅保留枚举非空断言），并在 DESIGN 注明降级路径。
- 白盒补齐：接口、extends `AutoCloseable`、11 个方法齐全。
