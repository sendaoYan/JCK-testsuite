# java.lang.annotation.RetentionPolicy — 设计方案（JDK 8）

## 1. API 功能
枚举：`@Retention` 的取值域。
- 3 个常量：SOURCE（编译丢弃）、CLASS（保留到 classfile，反射不可见，默认）、RUNTIME（反射可见）
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME) @interface A {}
switch (policy) { case SOURCE: ... case CLASS: ... case RUNTIME: ... }
```

## 3. 开源代码实际用法调研
- JDK `RetentionPolicy.java`：CLASS 为无 @Retention 时的默认。
- Lombok 用 SOURCE/CLASS；Spring/Jakarta 用 RUNTIME。

## 4. 测试用例设计 + 自动化实现
- 结构：枚举、父类 Enum、3 常量、values/valueOf 存在。
- `values()` 恰 3 个且顺序 SOURCE→CLASS→RUNTIME；防御性拷贝。
- 三常量 valueOf 往返；大小写敏感抛 IAE；每个常量 ordinal == 下标（0/1/2）。
- compareTo/name/toString；switch 消费范式；== 身份。

## 易错规范细节（JDK 8）
- 3 个常量在 JDK 8 与 JDK 27 完全一致，可双侧镜像断言。
