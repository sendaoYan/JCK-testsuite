# javax.lang.model.SourceVersion — 设计方案

## 1. API 功能
枚举：JDK 源码版本（RELEASE_0…RELEASE_27）及 latest/latestSupported/isIdentifier/isName/isKeyword/valueOf(Runtime.Version) 等工具。

## 2. Java 用户如何使用
`SourceVersion.latestSupported()` 判断当前编译能力；`isIdentifier/isKeyword` 做词法校验。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/SourceVersion.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言 28 个常量；黑盒断言 latest==latestSupported==RELEASE_27、isIdentifier/isName/isKeyword 判定与 runtimeVersion。 对应 `SourceVersionTest.java` 的 `testBlackBox()`。
