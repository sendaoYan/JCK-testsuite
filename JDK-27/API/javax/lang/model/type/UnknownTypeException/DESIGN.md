# javax.lang.model.type.UnknownTypeException — 设计方案

## 1. API 功能
异常：未知类型种类时抛出，携带 TypeMirror 与参数。

## 2. Java 用户如何使用
访问器 visitUnknown 场景抛 `UnknownTypeException(t, p)`。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/type/UnknownTypeException.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言继承 UnknownEntityException；黑盒用编译器取真实类型构造并断言 getter。 对应 `UnknownTypeExceptionTest.java` 的 `testBlackBox()`。
