# javax.annotation.processing.SupportedAnnotationTypes — 设计方案

## 1. API 功能
注解：处理器声明所支持注解类型/选项/源码版本；Generated 标注生成代码。保留策略见下。

## 2. Java 用户如何使用
标注在处理器/生成类上。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/annotation/processing/SupportedAnnotationTypes.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言注解类型与 Annotation 父类；黑盒反射断言保留策略（前三个 RUNTIME、Generated SOURCE）与 value/date/comments 默认值。 对应 `SupportedAnnotationTypesTest.java` 的 `testBlackBox()`。
