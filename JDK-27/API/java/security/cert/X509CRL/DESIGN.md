# java.security.cert.X509CRL 黑盒行为测试设计

## 1. API 功能
X.509 CRL：版本/sig 算法/this/next update/revoked 集合/TBSCertList/签名/verify/getRevokedCertificate(serial)/isRevoked + X509Extension。

## 2. Java 用户如何使用
吊销检查的核心读取面。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509CRL/*.java。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：v1→getVersion==1；issuer==jck-root；thisUpdate/nextUpdate 固定；getRevokedCertificate(BigInteger.ZERO) 命中且 getRevocationDate 可读、getRevocationReason 为 CRLReason；未吊销 serial 返回 null；verify(root) 成功/错钥 SignatureException；扩展 OID 集合为 null（无扩展）；equals/hashCode 稳定。 追加：getIssuerDN 弃用 Principal、getTBSCertList/getSignature 非空。

断言入口：`X509CRLTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
