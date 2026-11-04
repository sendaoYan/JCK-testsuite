# java.lang.annotation.Retention — 设计方案（JDK 8）

## 1. API 功能
元注解：控制注解保留策略。
- 唯一成员 `RetentionPolicy value()`（无默认，必须显式指定）
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME) @interface Cacheable { } // 反射可见
@Retention(RetentionPolicy.SOURCE)  @interface Generated { } // 编译后丢弃
```
SOURCE/CLASS 运行期反射不可见；RUNTIME 可见。

## 3. 开源代码实际用法调研
- JDK `Retention.java`：声明 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`。
- Spring/Jakarta 注解显式 RUNTIME；Lombok 用 SOURCE/CLASS。

## 4. 测试用例设计 + 自动化实现
- 结构：注解类型、单成员 `value()` 无默认。
- 元注解：Retention 自身 @Retention(RUNTIME)、@Target(ANNOTATION_TYPE)、@Documented。
- 同一类上并排 SOURCE/CLASS/RUNTIME：SOURCE/CLASS 用法运行期不可见、RUNTIME 可见、getAnnotations 只剩 2 个。
- 成员值经保留策略往返；未标 @Retention 的注解默认 CLASS（反射不可见）。

## 易错规范细节（JDK 8）
- 无 @Retention 时默认 CLASS：classfile 中保留但运行期反射不可见。
- `@Retention` 本身必须 RUNTIME 保留，否则任何策略都不可见。
