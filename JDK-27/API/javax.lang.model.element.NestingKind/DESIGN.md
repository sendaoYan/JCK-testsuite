# javax.lang.model.element.NestingKind — 设计方案

## 1. API 功能
枚举：类型嵌套层级（TOP_LEVEL/MEMBER/LOCAL/ANONYMOUS）及 isNested 判定。

## 2. Java 用户如何使用
`TypeElement.getNestingKind()` 判断顶层/成员/局部/匿名类。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/element/NestingKind.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言 4 常量；黑盒断言 isNested（TOP_LEVEL false，其余 true）与 valueOf。 对应 `NestingKindTest.java` 的 `testBlackBox()`。
