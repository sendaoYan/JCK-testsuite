# javax.sound.midi.ControllerEventListener — DESIGN

## 1. API 功能
`ControllerEventListener` 是标记/回调接口，继承 `java.util.EventListener`，用于接收 MIDI 控制器变化事件。方法：`void controlChange(ShortMessage event)`——当已注册的控制器发生控制变化时被调用，参数是携带控制变化消息的 `ShortMessage`。

## 2. Java 用户如何使用
用户实现 `ControllerEventListener`（通常用 lambda），并通过 `Sequencer.addControllerEventListener(listener, int[])` 注册感兴趣的控制器号；当对应控制器变化时，`controlChange(ShortMessage)` 被回调。典型链路：`ShortMessage msg = new ShortMessage(); msg.setMessage(CONTROL_CHANGE, 0, 7, 100)` → 实现监听器 → `listener.controlChange(msg)` → 在回调中读取 `msg.getData1()/getData2()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Sequencer.java`：`addControllerEventListener(ControllerEventListener, int[])` 与 `removeControllerEventListener(...)` 定义该接口的使用入口。
- `src/java.desktop/share/classes/com/sun/media/sound/RealTimeSequencer.java`：内部维护 controller 事件监听器列表并在处理控制变化时派发回调。
- `test/jdk/javax/sound/midi/Sequencer/MetaCallback.java`（同类监听器模式）：演示 `MetaEventListener` 通过 `addMetaEventListener` 注册、回调中读取消息数据。
- 该接口是纯监听器约定，不依赖 MIDI 设备即可实例化并验证回调签名。

## 4. 测试用例设计 + 自动化实现（`ControllerEventListenerTest.testBlackBox`）
- 白盒：断言 isInterface、继承 `EventListener`、仅 1 个抽象方法 `controlChange(ShortMessage)`、返回 `void`。
- 黑盒：构造真实 `ShortMessage`（CONTROL_CHANGE 通道 0 控制器 7 值 100），用 lambda 实现监听器，调用 `controlChange(msg)` 断言回调收到同一 `ShortMessage` 实例且调用次数准确，连续调用计数累加。无需 MIDI 设备，headless 确定。
