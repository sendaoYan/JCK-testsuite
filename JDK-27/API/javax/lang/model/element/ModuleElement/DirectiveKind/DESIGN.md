# javax.lang.model.element.ModuleElement.DirectiveKind — 设计方案

## 1. API 功能
枚举：模块指令种类（REQUIRES/EXPORTS/OPENS/USES/PROVIDES）。

## 2. Java 用户如何使用
遍历 `ModuleElement.getDirectives()` 时按 `getKind()` 分支。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/element/ModuleElement.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言 5 常量；黑盒断言 valueOf/ordinal。 对应 `DirectiveKindTest.java` 的 `testBlackBox()`。
