# java.security.KeyStore.Entry 黑盒行为测试设计

## 1. API 功能
- 接口：`getAttributes()`；实现类 SecretKeyEntry/PrivateKeyEntry/TrustedCertificateEntry

## 2. Java 用户如何使用
统一条目抽象：`ks.getEntry(alias, prot)` 返回 Entry，按 instanceof 分派。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（三个 final 实现类）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`（Entry 分派与保护参数）

## 4. 测试用例设计 + 自动化实现
- 正常：直接构造的三类条目 getAttributes 为空
- 组合：三类条目进同一 PKCS12 库后 getEntry 按类型还原；缺失别名 null；toString 非空

断言入口：`EntryTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
