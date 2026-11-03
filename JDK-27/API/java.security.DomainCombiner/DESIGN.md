# java.security.DomainCombiner 黑盒行为测试设计

## 1. API 功能
- 抽象方法：`combine(ProtectionDomain[], ProtectionDomain[])`

## 2. Java 用户如何使用
实现者在 doPrivileged 时合并"当前域"与"特权域"；经 `new AccessControlContext(acc, combiner)` 装配，`getDomainCombiner()` 读回。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/DomainCombiner.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/AccessControlContext/NullCombinerEquals.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/SecurityManager/Basic.java`

## 4. 测试用例设计 + 自动化实现
- 黑盒：lambda 实现 combine 返回入参的固定组合；经 ACC 构造后 getDomainCombiner 读回同一实例；combine 被调用时入参可观测（记录后断言）

断言入口：`DomainCombinerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
