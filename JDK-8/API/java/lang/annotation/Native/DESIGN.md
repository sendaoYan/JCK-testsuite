# java.lang.annotation.Native — 设计方案（JDK 8）

## 1. API 功能
标记注解（无成员）：指示常量字段值可能来自 native 代码（供工具生成头文件使用）。
- 声明 `@Documented @Retention(SOURCE) @Target(FIELD)`
- SOURCE 保留意味着使用处在编译后即丢弃。

## 2. Java 用户如何使用
```java
class Constants {
    @Native public static final int SIGSEGV = 11; // javah/jextract 类工具识别
}
```
运行期反射不可见（SOURCE 策略），仅供编译期/工具期消费。

## 3. 开源代码实际用法调研
- JDK `Native.java`：javadoc“常量值可能来自 native”。
- JDK `Integer.MAX_VALUE`、`Double.MAX_VALUE` 等标 `@Native`（源码可见）。
- JNR/JNoDetect 常量生成器消费该标记。

## 4. 测试用例设计 + 自动化实现
- 结构：注解类型、零成员、元注解 @Retention(SOURCE)+@Target(FIELD)+@Documented 可回读。
- `@Native` 字段用法运行期不可见、`getAnnotations()` 为空；对照字段无注解。
- 手写实例 annotationType；JDK `Integer.MAX_VALUE` 用法运行期同样不可见。

## 易错规范细节（JDK 8）
- `@Native` 自身元注解（@Retention/@Target/@Documented）是 RUNTIME 保留的，因此可在 `Native.class` 上反射读取；但 `@Native` 的使用处是 SOURCE，运行期不可见。
