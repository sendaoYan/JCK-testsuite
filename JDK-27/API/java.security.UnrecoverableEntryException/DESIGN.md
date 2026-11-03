# java.security.UnrecoverableEntryException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`；子类 UnrecoverableKeyException

## 2. Java 用户如何使用
KeyStore.getEntry 口令错误/条目损坏时抛出。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/UnrecoverableEntryException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`（getEntry 错误口令路径）

## 4. 测试用例设计 + 自动化实现
- 正常：无参/带 message 构造精确断言
- 组合：PKCS12 内存库错口令 getEntry → UnrecoverableKeyException（子类）instanceof 本类
- 边界：子类多态 catch 顺序

断言入口：`UnrecoverableEntryExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
