# javax.lang.model.UnknownEntityException — 设计方案

## 1. API 功能
抽象异常基类：未知实体异常的公共父类（RuntimeException）。

## 2. Java 用户如何使用
子类化并传消息。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/UnknownEntityException.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言继承 RuntimeException 与 protected 构造器；黑盒子类化断言 getMessage。 对应 `UnknownEntityExceptionTest.java` 的 `testBlackBox()`。
