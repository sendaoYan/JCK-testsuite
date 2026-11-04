# java.security.cert.TrustAnchor 黑盒行为测试设计

## 1. API 功能
信任锚：ctor(X509Certificate,byte[] nc)/ctor(X500Principal,PublicKey,byte[])/ctor(String,PublicKey,byte[])，getTrustedCert/getCA/getCAName/getCAPublicKey/getNameConstraints。

## 2. Java 用户如何使用
PKIX 验证的信任根配置（证书形态或 CA 名+密钥形态）。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/trustAnchor/ValWithAnchorByName.java（名+密钥形态真实用例）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：三种 ctor 的字段互补性（cert 形态 CA 域为 null，CA 形态 trustedCert 为 null，name 形态 principal 等价）；无 equals 覆盖（identity 语义，实测）+ hashCode==identityHashCode；name constraints 原样保留（有效 DER 空 SEQUENCE）、坏 DER 抛 IllegalArgumentException；各 null 参数 NPE（注意 (X500Principal,null,..) 与 (String,null,..) 重载需显式转型避免歧义，实测）。

断言入口：`TrustAnchorTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
