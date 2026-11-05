# javax.sound.sampled.LineEvent.Type — DESIGN

## 1. API 功能
`LineEvent.Type` 是 `LineEvent` 的静态嵌套类，标识 Line 事件的类型。方法分组：
- 常量：`OPEN`、`CLOSE`、`START`、`STOP`。
- 构造器（受保护）：`Type(String name)`。
- 查询：`toString()`（返回 name）。
- 相等：`equals`/`hashCode` 为 final、身份语义。

## 2. Java 用户如何使用
用户引用 `LineEvent.Type.START` 等常量构造或判断事件类型；监听器 `LineListener.update` 内比较 `event.getType() == LineEvent.Type.OPEN`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/LineEvent.java:221`：`OPEN = new Type("Open")`、`CLOSE = new Type("Close")`、`START = new Type("Start")`、`STOP = new Type("Stop")`。
- `src/java.desktop/share/classes/javax/sound/sampled/LineEvent.java:187`：`equals` 返回 `super.equals(obj)`（身份语义）。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- 4 个常量非空互异；`toString()` 为 `Open/Close/Start/Stop`。
- 身份相等：自反、不同常量不等、与 null 不等、`hashCode` 稳定。
