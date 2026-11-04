# javax.annotation.processing.FilerException — 设计方案

## 1. API 功能
异常：注解处理器写文件冲突/非法时抛出的 IOException。

## 2. Java 用户如何使用
`catch (FilerException e)`。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/annotation/processing/FilerException.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言继承 IOException；黑盒构造断言 getMessage。 对应 `FilerExceptionTest.java` 的 `testBlackBox()`。
