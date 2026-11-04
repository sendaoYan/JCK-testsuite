# java.lang.annotation.Native — 设计方案（JDK 27）

## 1. API 功能
标记注解：指示常量字段值可能来自 native 代码。
- 声明 `@Documented @Retention(SOURCE) @Target(FIELD)`

## 2. Java 用户如何使用
```java
class Constants { @Native public static final int SIGSEGV = 11; }
```
运行期反射不可见（SOURCE）。

## 3. 开源代码实际用法调研
- JDK `Native.java`；`Integer.MAX_VALUE` 等标 @Native。
- JNR/JNoDetect 常量生成器。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、零成员、@Retention(SOURCE)+@Target(FIELD)+@Documented 可回读。
黑盒：@Native 字段用法运行期不可见、getAnnotations 为空；对照字段无；手写实例 annotationType；JDK Integer.MAX_VALUE 用法不可见。

## 易错规范细节（JDK 27）
- @Native 的元注解 RUNTIME 保留可反射读取；@Native 使用处 SOURCE，运行期不可见。
