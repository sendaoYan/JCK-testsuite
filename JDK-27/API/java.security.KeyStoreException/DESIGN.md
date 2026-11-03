# java.security.KeyStoreException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`

## 2. Java 用户如何使用
KeyStore 尚未加载（`ks.size()` 等）或类型不存在时抛出。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStoreException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/TestKeyStoreBasic.java`（未加载即用抛 KeyStoreException）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/misc/GetInstanceNullsEmpties.java`（getInstance 参数校验）

## 4. 测试用例设计 + 自动化实现
- 正常：4 构造器 message/cause 精断
- 组合：`KeyStore.getInstance("PKCS12")` 后立即 size() 抛 KeyStoreException；`getInstance("Nope")` 抛同类型
- 边界：异常链 toString 包含 cause 类名

断言入口：`KeyStoreExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
