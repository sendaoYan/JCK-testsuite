# javax.lang.model.type.TypeKind — 设计方案

## 1. API 功能
枚举：类型种类（INT/ARRAY/DECLARED/TYPEVAR/WILDCARD/…）及 isPrimitive 判定。

## 2. Java 用户如何使用
按 `TypeMirror.getKind()` 分支处理类型；`TypeKind.isPrimitive()` 区分基本类型。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/type/TypeKind.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言 22 个常量子集；黑盒断言 isPrimitive（8 个基本类型 true）、valueOf、ordinal。 对应 `TypeKindTest.java` 的 `testBlackBox()`。
