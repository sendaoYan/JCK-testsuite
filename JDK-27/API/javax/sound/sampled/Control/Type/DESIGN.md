# javax.sound.sampled.Control.Type — DESIGN

## 1. API 功能
`Control.Type` 是 `Control` 的静态嵌套类，是所有控件类型的基类。方法分组：
- 构造器（受保护）：`Type(String name)`。
- 查询：`toString()`（返回 name）。
- 相等：`equals`/`hashCode` 为 final、身份语义（`super.equals`），与名字无关。

## 2. Java 用户如何使用
用户子类化以定义控件类型：`new Control.Type("Gain") {}`。由于相等为身份语义，两个名字相同但不同的实例不相等；控件实现通常用单例静态常量。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:95`：受保护构造器 `Type(String name)`。
- `src/java.desktop/share/classes/javax/sound/sampled/Control.java:107`：`equals` 返回 `super.equals(obj)`（身份比较）。

## 4. 测试用例设计 + 自动化实现（`TypeTest.testBlackBox`）
- 匿名子类实例化：`toString()` 名字往返。
- 身份相等：同名不同实例不相等、自反、不同名不等、与 null 不等、`hashCode` 稳定。
