# javax.lang.model.type.MirroredTypesException — 设计方案

## 1. API 功能
异常：读取注解中的 Class 值时抛出的镜像类型异常，携带 TypeMirror/TypeMirror 列表。

## 2. Java 用户如何使用
`MirroredTypeException` 捕获后 `getTypeMirror()` 取得注解引用的类型。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/type/MirroredTypesException.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言继承关系；黑盒用编译器取真实 TypeMirror 构造并断言 getTypeMirror/getTypeMirrors。 对应 `MirroredTypesExceptionTest.java` 的 `testBlackBox()`。
