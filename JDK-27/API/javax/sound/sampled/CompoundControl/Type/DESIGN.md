# javax.sound.sampled.CompoundControl.Type — DESIGN

## 1. API 功能
`CompoundControl.Type` 是 `CompoundControl` 的静态嵌套类，继承 `Control.Type`，标识复合控件类型。方法分组：
- 构造器（受保护）：`Type(String name)`。
- 继承自 `Control.Type` 的 `toString()` 与身份 `equals/hashCode`。
- 注意：本类没有公开静态常量（与 BooleanControl.Type 等不同），类型由具体实现子类化定义。

## 2. Java 用户如何使用
用户通过匿名子类定义类型：`new CompoundControl.Type("Equalizer") {}`，再传给 `CompoundControl` 构造器。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/CompoundControl.java:87`：`Type` 仅提供受保护构造器 `super(name)`，不定义常量。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:95`：`Control.Type(String)` 为受保护构造器，`toString()` 返回 name，`equals/hashCode` 为身份语义。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- 匿名子类实例化两个类型，`toString()` 名字往返、均为 `Control.Type`。
- 身份相等：自反、不同实例不等、与 null 不等、`hashCode` 稳定。
