# javax.sound.sampled.BooleanControl.Type — DESIGN

## 1. API 功能
`BooleanControl.Type` 是 `BooleanControl` 的静态嵌套类，继承 `Control.Type`，标识布尔控件类型。方法分组：
- 常量：`MUTE`、`APPLY_REVERB`。
- 构造器（受保护）：`Type(String name)`。
- 继承自 `Control.Type` 的 `toString()`（返回 name）与身份 `equals/hashCode`。

## 2. Java 用户如何使用
用户把内置常量 `BooleanControl.Type.MUTE` 传给 `BooleanControl` 构造器；子类化以扩展自定义类型。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/BooleanControl.java:145`：`MUTE = new Type("Mute")`、`APPLY_REVERB = new Type("Apply Reverb")`。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:128`：`Control.Type.toString()` 返回 name；`equals/hashCode` 为身份语义（`super.equals`）。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- 常量非空互异；`toString()` 为 `Mute`/`Apply Reverb`；均为 `Control.Type` 实例。
- 身份相等：自反、与另一类型不等、与 null 不等、`hashCode` 稳定。
