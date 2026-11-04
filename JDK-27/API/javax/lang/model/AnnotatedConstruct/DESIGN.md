# javax.lang.model.AnnotatedConstruct — 设计方案

## 1. API 功能
接口：带注解的构造（元素/类型）的注解读取能力。

## 2. Java 用户如何使用
`getAnnotationMirrors/getAnnotation/getAnnotationsByType` 读取注解。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/AnnotatedConstruct.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言接口方法；黑盒解析含 @Deprecated 的类断言注解镜像与 getAnnotation。 对应 `AnnotatedConstructTest.java` 的 `testBlackBox()`。
