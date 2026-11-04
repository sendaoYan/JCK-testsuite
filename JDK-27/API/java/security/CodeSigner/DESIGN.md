# CodeSigner — 设计案

## 1. API 功能
不可变签名者（implements Serializable）。构造器(CertPath signerCertPath, Timestamp timestamp) + getSignerCertPath/getTimestamp + equals/hashCode/toString。

## 2. 使用方式
`new CodeSigner(证书链 CertPath, new Timestamp(date, tsCertPath))`。

## 3. 参考来源
- javadoc `java.security.CodeSigner`。
- JCK runtime-8b `api/java_security/CodeSigner/`。

## 4. 测试用例设计 + 易错规范细节
- 构造器 null signerCertPath→NPE；null timestamp 合法（getTimestamp 返回 null）。
- getSignerCertPath/getTimestamp 返回传入值。
- equals/hashCode 值语义；不同 timestamp → false；null timestamp 与 null 比较。
- 序列化往返保留 signerCertPath 与 timestamp。
- 易错点：第二参是 Timestamp 而非 Date（构造器签名 CodeSigner(CertPath, Timestamp)）。
