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

## 契约深化（2026-11）
### API 功能
- 负例精确消息（与 JDK-8 逐字一致）；@Retention(RUNTIME) 自反可见消费闭环；单例共享/getDeclaringClass/compareTo 精确差值/三名串全等。

### 使用方式
同 JDK-8 侧。

### 参考来源
Enum.valueOf；javap（jdk-27.0.1）。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testNegativeExactAndIdentity`；双侧无版本差异（枚举集两侧均为 3 值）。
