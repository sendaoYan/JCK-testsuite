# javax.sound.sampled.FloatControl.Type — DESIGN

## 1. API 功能
`FloatControl.Type` 是 `FloatControl` 的静态嵌套类，继承 `Control.Type`，标识浮点控件类型。方法分组：
- 常量：`MASTER_GAIN`、`AUX_SEND`、`AUX_RETURN`、`REVERB_SEND`、`REVERB_RETURN`、`VOLUME`、`PAN`、`BALANCE`、`SAMPLE_RATE`。
- 构造器（受保护）：`Type(String name)`。
- 继承自 `Control.Type` 的 `toString()` 与身份 `equals/hashCode`。

## 2. Java 用户如何使用
用户把 `FloatControl.Type.MASTER_GAIN` 等常量传给 `FloatControl` 构造器或 `Line.isControlSupported/getControl` 做类型查询。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/FloatControl.java:390`：`MASTER_GAIN = new Type("Master Gain")` 等 9 个常量定义（如 `AUX_SEND = "AUX Send"`、`SAMPLE_RATE = "Sample Rate"`）。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:128`：`toString()` 返回 name；`equals/hashCode` 身份语义。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- 9 个常量非空、互异、均为 `Control.Type` 实例。
- `toString()` 逐一断言 9 个名字。
- 身份相等：自反、不同常量不等、与 null 不等、`hashCode` 稳定。
