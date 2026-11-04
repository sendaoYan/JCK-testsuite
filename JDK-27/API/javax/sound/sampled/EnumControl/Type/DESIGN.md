# javax.sound.sampled.EnumControl.Type — DESIGN

## 1. API 功能
`EnumControl.Type` 是 `EnumControl` 的静态嵌套类，继承 `Control.Type`，标识枚举控件类型。方法分组：
- 常量：`REVERB`（每个设置由 `ReverbType` 描述）。
- 构造器（受保护）：`Type(String name)`。
- 继承自 `Control.Type` 的 `toString()` 与身份 `equals/hashCode`。

## 2. Java 用户如何使用
用户把 `EnumControl.Type.REVERB` 传给 `EnumControl` 构造器，再通过 `getValues()` 获取各个 `ReverbType` 设置。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/EnumControl.java:161`：`REVERB = new Type("Reverb")`。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:128`：`toString()` 返回 name；`equals/hashCode` 为身份语义。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- `REVERB` 非空、`toString()` 为 `Reverb`、是 `Control.Type` 实例。
- 身份相等：自反、与 `FloatControl.Type.VOLUME` 不等、与 null 不等、`hashCode` 稳定。
