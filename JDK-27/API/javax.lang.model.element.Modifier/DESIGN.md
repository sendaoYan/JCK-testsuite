# javax.lang.model.element.Modifier — 设计方案

## 1. API 功能
枚举：语言修饰符（PUBLIC/ABSTRACT/STATIC/SEALED/NON_SEALED/…）及 `toString()` 返回规范小写/连字符名。

## 2. Java 用户如何使用
`element.getModifiers().contains(Modifier.PUBLIC)` 判断修饰符；`Modifier.toString()` 输出 "public"/"non-sealed"。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/element/Modifier.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言 14 个常量与 Enum 形态；黑盒断言 toString（public/non-sealed/strictfp）、valueOf、ordinal。 对应 `ModifierTest.java` 的 `testBlackBox()`。
