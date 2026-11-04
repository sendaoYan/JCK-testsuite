# java.security.Identity 黑盒行为测试设计

## 1. API 功能
- 构造：`Identity(String)`、`Identity(String, IdentityScope)`（protected）
- 操作：`setPublicKey`/`setInfo`（JDK-27 为无条件 setter）、`addCertificate`/`removeCertificate`（抛 KeyManagementException）
- 查询：`getName/getPublicKey/getInfo/getScope/toString`、`identityEquals`、`equals/hashCode`（final）

## 2. Java 用户如何使用
JDK 1.1 遗留签名体系；equals 按 `name.scope` fullName 判定，同名同 scope 无视密钥即相等。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Identity.java`（equals final、identityEquals 默认实现）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/Identity/EqualsHashCodeContract.java`（equals/hashCode 契约）

## 4. 测试用例设计 + 自动化实现
- 正常：name/scope 读写、info 默认非 null、setInfo 透传
- 组合：equals 矩阵（同名等价、不同名不等、identityEquals 的密钥 null 异或规则）、hashCode 恒为 name.hashCode
- 行为差异：JDK-27 setPublicKey 是无条件替换（不同密钥/null 均静默接受）；addCertificate 抛 KeyManagementException

断言入口：`IdentityTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
