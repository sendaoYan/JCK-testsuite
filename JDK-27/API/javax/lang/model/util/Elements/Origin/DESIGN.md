# javax.lang.model.util.Elements.Origin — 设计方案

## 1. API 功能
枚举：Elements 文档注释种类（END_OF_LINE/TRADITIONAL）或元素来源（EXPLICIT/MANDATED/SYNTHETIC）及判定方法。

## 2. Java 用户如何使用
`Elements.DocCommentKind.values()`/`valueOf`；`Origin.isDeclared()` 判断是否显式声明（!= SYNTHETIC）。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/util/Elements.java`（嵌套枚举定义于此）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言枚举常量子集；黑盒断言 values/valueOf/ordinal 与 `isDeclared` 语义（EXPLICIT/MANDATED true，SYNTHETIC false）。 对应 `OriginTest.java` 的 `testBlackBox()`。
