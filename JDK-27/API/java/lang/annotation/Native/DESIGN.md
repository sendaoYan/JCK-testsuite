# java.lang.annotation.Native — 设计方案

## 1. API 功能
标记注解（无成员）：指示常量字段值来自 native 代码（供工具生成头文件时使用）：
- 声明 `@Documented @Retention(SOURCE) @Target(ElementType.FIELD)`
- SOURCE 保留意味着使用处在编译后即丢弃

## 2. Java 用户如何使用
```java
class Constants {
    @Native public static final int SIGSEGV = 11;  // javah/jextract 类工具识别
}
```
运行期反射不可见（SOURCE 策略），仅供编译期/工具期消费。

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/Native.java`：javadoc 说明“常量值可能来自 native”。
- JDK 大量使用：`Integer.MAX_VALUE`、`Double.MAX_VALUE` 等公共常量均标 `@Native`（源码可见，如 `java/lang/Integer.java`）。
- Eclipse JNoDetect/JNR 常量生成器消费该标记区分 JNI 常量。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、零成员、元注解 `@Retention(SOURCE)`+`@Target(FIELD)` 可回读（元注解自身 RUNTIME 保留）。
黑盒：
- `@Native` 用法在字段上运行期不可见（SOURCE 策略的直接后果）、`getAnnotations()` 为空；
- 对照字段无注解；
- 手写实例的 annotationType/toString；
- JDK 自家 `Integer.MAX_VALUE` 上的用法运行期同样不可见。
对应 `NativeTest.java` 的 `testBlackBox()`。
