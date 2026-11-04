# javax.sound.midi.MetaEventListener — DESIGN

## 1. API 功能
`MetaEventListener` 是回调接口，继承 `java.util.EventListener`，用于接收 MIDI 元事件（meta event）。方法：`void meta(MetaMessage meta)`——当已注册的元事件到达时被调用，参数为 `MetaMessage`（如速度、歌词、版权、拍号、调号等）。

## 2. Java 用户如何使用
用户实现 `MetaEventListener`（通常 lambda），通过 `Sequencer.addMetaEventListener(listener)` 注册；当 sequencer 处理到元事件时回调 `meta(MetaMessage)`。典型链路：构造 `MetaMessage`（如 tempo 事件 `setMessage(0x51, data, 3)`）→ 实现监听器 → `listener.meta(msg)` → 回调中读取 `msg.getType()/getData()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Sequencer.java`：`addMetaEventListener(MetaEventListener)` 与 `removeMetaEventListener(...)` 定义使用入口。
- `test/jdk/javax/sound/midi/Sequencer/MetaCallback.java`：真实注册 `MetaEventListener`，在 `meta(MetaMessage)` 回调中读取事件类型并计数。
- `src/java.desktop/share/classes/com/sun/media/sound/RealTimeSequencer.java`：维护 meta 监听器列表并在读取/回放元事件时派发回调。
- 该接口是纯监听器约定，不依赖 MIDI 设备即可实例化并验证回调签名。

## 4. 测试用例设计 + 自动化实现（`MetaEventListenerTest.testBlackBox`）
- 白盒：断言 isInterface、继承 `EventListener`、仅 1 个抽象方法 `meta(MetaMessage)`、返回 `void`。
- 黑盒：构造真实 tempo `MetaMessage`（`setMessage(0x51, data, 3)`），用 lambda 实现监听器，调用 `meta(msg)` 断言回调收到同一 `MetaMessage` 实例、调用次数准确、连续调用累加。无需 MIDI 设备，headless 确定。
