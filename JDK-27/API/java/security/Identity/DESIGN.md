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

## 5. 深化补遗（testContractMatrix，实测修正与新增维度）
- **修正 §2 的 equals 描述**：实测 JDK-27 与 JDK-8 一致——equals/hashCode **只按 name**（同名跨 scope、跨密钥状态均相等），不是 name.scope fullName；scope 与 key 均不参与。
- 新增矩阵：null 名构造合法（getName null、toString() 返回 null 字符串值 null）；null 名身份的 equals → NPE；getName/getScope/equals 均为 final；protected 无参构造。
- 证书矩阵：初始 certificates() 空**数组**非 null；addCertificate(null)→NPE；cert 公钥与身份公钥不一致→KME "public key different from cert public key"；重复 add 允许（保留重复项）；removeCertificate 缺失/null 均静默；失败 add 不改变列表长度。
- 通知边界：scope 的 addIdentity 只在构造时被调用一次；setPublicKey/setPublicKey(null)/addCertificate/removeCertificate 一概不再通知。
- 严格 scope：重复名构造从 ctor 直通 scope 抛出的 KeyManagementException。
- identityEquals（protected，经命名子类暴露）：同名同钥 true；null 钥不对称 false；异名 false。
- toString()：恒等于 name；toString(true) 含 "no public key"/"public key initialized"/"no certificates"/info。
- 序列化 round-trip：name/info/公钥/证书全保留且 equals 原对象。
