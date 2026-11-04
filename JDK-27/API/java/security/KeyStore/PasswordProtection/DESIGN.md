# java.security.KeyStore.PasswordProtection 黑盒行为测试设计

## 1. API 功能
- 构造：`PasswordProtection(char[])`、`PasswordProtection(char[], String, AlgorithmParameterSpec)`
- 查询：`getPassword()`、`getProtectionAlgorithm()`、`getProtectionParameters()`
- 生命周期：`destroy()`、`isDestroyed()`

## 2. Java 用户如何使用
口令保护的载体；destroy 后 getPassword 抛 IllegalStateException。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（getPassword 返回内部数组不克隆；destroyed 状态检查）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`

## 4. 测试用例设计 + 自动化实现
- 正常：构造时防御拷贝（外部改数组不影响）；getProtectionAlgorithm/Parameters 默认 null、3 参构造记录算法
- 行为记录：getPassword 返回内部数组（读侧无克隆，JDK-27 源码可见）
- 生命周期：destroy → isDestroyed=true、getPassword IllegalStateException；null 口令可保留

断言入口：`PasswordProtectionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
