# javax.sound.sampled.LineEvent — DESIGN

## 1. API 功能
`LineEvent` 继承 `EventObject`，封装一条 Line 打开/关闭/开始/停止时的状态变化事件。方法分组：
- 构造器：`LineEvent(Line line, Type type, long position)`（line 为 null 抛 `IllegalArgumentException`）。
- 查询：`getLine()`（返回 `(Line) getSource()`）、`getType()`、`getFramePosition()`（采样帧位置，未知时为 `AudioSystem.NOT_SPECIFIED`）。
- 工具：`toString()`（格式 `"<type> event from line <line>"`）。

## 2. Java 用户如何使用
监听器收到事件后调用 `getType()` 判断 OPEN/CLOSE/START/STOP、`getLine()` 定位来源、`getFramePosition()` 得到媒体位置。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/LineEvent.java:93`：构造器 `super(line)` 存入 source。
- `src/java.desktop/share/classes/javax/sound/sampled/LineEvent.java:105`：`getLine()` 返回 `(Line) getSource()`。
- `src/java.desktop/share/classes/javax/sound/sampled/LineEvent.java:150`：`toString()` 格式 `"%s event from line %s"`。

## 4. 测试用例设计 + 自动化实现（`LineEventTest.testBlackBox`）
- 用 `MockLine` 构造 `START` 事件：`getLine()/getSource()` 身份、`getType()`、`getFramePosition()`。
- `OPEN` 事件与位置 0。
- `toString()` 含类型名与 line 名。
