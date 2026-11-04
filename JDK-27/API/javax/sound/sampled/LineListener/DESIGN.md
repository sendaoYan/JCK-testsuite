# javax.sound.sampled.LineListener — DESIGN

## 1. API 功能
`LineListener` 是监听 Line 状态变化的监听器接口，继承 `java.util.EventListener`。方法分组：
- 单一方法：`update(LineEvent event)`（Line 状态变化时被调用）。

## 2. Java 用户如何使用
用户注册监听器后，实现 `update` 接收 `LineEvent` 并查询类型/来源/位置。由于只有单个抽象方法，可用 lambda：`line.addLineListener(e -> count++)`，也可匿名类实现以保留事件。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/LineListener.java:41`：`public interface LineListener extends EventListener`，唯一方法 `void update(LineEvent event)`。
- `src/java.desktop/share/classes/javax/sound/sampled/Line.java:182`：`addLineListener(LineListener)` 在状态变化时以 `LineEvent` 调用 `update`。

## 4. 测试用例设计 + 自动化实现（`LineListenerTest.testBlackBox`）
- lambda 实现：`instanceof LineListener` 与 `EventListener`；多次 `update` 计数。
- 匿名类实现：收到同一个事件，能读取 `getType()/getFramePosition()`。
