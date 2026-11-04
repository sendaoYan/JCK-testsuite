# java.lang.annotation.RetentionPolicy — 设计方案（JDK 27）

## 1. API 功能
枚举：`@Retention` 取值域。
- 3 常量：SOURCE、CLASS（默认）、RUNTIME
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME) @interface A {}
switch (policy) { case SOURCE: ... case CLASS: ... case RUNTIME: ... }
```

## 3. 开源代码实际用法调研
- JDK `RetentionPolicy.java`：CLASS 为无 @Retention 默认。
- Lombok SOURCE/CLASS；Spring/Jakarta RUNTIME。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、父类 Enum、3 常量、values/valueOf 存在。
黑盒：values() 恰 3 个且顺序 SOURCE→CLASS→RUNTIME；防御性拷贝；三常量 valueOf 往返；大小写敏感 IAE；ordinal 0/1/2；compareTo/name/toString；switch；== 身份。

## 易错规范细节（JDK 27）
- 3 常量在 JDK 8 与 JDK 27 一致。
