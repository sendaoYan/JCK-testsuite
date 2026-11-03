# javax.annotation.processing.AbstractProcessor — 设计方案

## 1. API 功能
抽象注解处理器：实现 Processor，从 @SupportedAnnotationTypes/@SupportedOptions/@SupportedSourceVersion 读取声明，管理 ProcessingEnvironment 与初始化状态。

## 2. Java 用户如何使用
继承并实现 `process`；`init` 注入环境；框架按 `getSupportedAnnotationTypes` 过滤注解。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/annotation/processing/AbstractProcessor.java`；`test/langtools/tools/javac/processing/` 处理器示例。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言抽象类实现 Processor；黑盒用注解/无注解两个子类断言 getSupported* 默认读取、init/isInitialized 状态机与重复 init 抛 IllegalStateException。 对应 `AbstractProcessorTest.java` 的 `testBlackBox()`。
