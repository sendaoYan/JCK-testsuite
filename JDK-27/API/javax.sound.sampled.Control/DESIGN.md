# javax.sound.sampled.Control — DESIGN

## 1. API 功能
`Control` 是所有音频控件（`BooleanControl`、`EnumControl`、`FloatControl`、`CompoundControl`）的抽象基类。方法分组：
- 构造器（受保护）：`Control(Type type)`。
- 查询：`getType()`。
- 工具：`toString()`（格式 `"<type> control"`）。
- 嵌套类 `Control.Type`：受保护构造器、`toString()` 返回 name、`equals/hashCode` 为身份语义。

## 2. Java 用户如何使用
用户不直接实例化 `Control`，而是子类化并用具体 `Type`：`new Control(new Control.Type("Gain") {}) {}`，再 `getType().toString()` 得到类型名。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:55`：受保护构造器 `this.type = type`。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:73`：`toString()` 返回 `String.format("%s control", getType())`。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:107`：`Type.equals/hashCode` 直接委托 `super`（身份语义）。

## 4. 测试用例设计 + 自动化实现（`ControlTest.testBlackBox`）
- 匿名子类实例化：`getType()` 身份、`Type.toString()` 名字、`Control.toString()` 含类型名。
- `Control.Type` 身份相等：两个同名实例不相等、自反、与 null 不等、`hashCode` 稳定。
- 不同类型名区分。
