# javax.lang.model.type.WildcardType — 设计方案

## 1. API 功能
类型模型接口：描述类型（基本/数组/声明/类型变量/通配/并集/交集等）的结构。

## 2. Java 用户如何使用
通过 `Types.getPrimitiveType/getArrayType/getDeclaredType` 构造，`asType/getKind/accept` 读取。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/type/WildcardType.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言接口继承；黑盒用 Types 工厂构造真实类型，断言 kind/component/bounds/accept 派发。 对应 `WildcardTypeTest.java` 的 `testBlackBox()`。
