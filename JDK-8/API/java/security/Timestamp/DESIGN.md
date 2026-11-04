# Timestamp — 设计案

## 1. API 功能
不可变时间戳（implements Serializable）。构造器(Date timestamp, CertPath signerCertPath) + getTimestamp/getSignerCertPath + equals/hashCode/toString。

## 2. 使用方式
`new Timestamp(new Date(...), 证书链 CertPath)`。

## 3. 参考来源
- javadoc `java.security.Timestamp`。
- JCK runtime-8b `api/java_security/Timestamp/`。

## 4. 测试用例设计 + 易错规范细节
- 构造器 null timestamp→NPE；null signerCertPath→NPE（两个 null 都拒绝）。
- getTimestamp/getSignerCertPath 返回传入值。
- equals/hashCode 值语义：比较 timestamp 与 signerCertPath 两者；不同 timestamp → false。
- 序列化往返保留 timestamp 与 signerCertPath。
- 易错点：CertPath.equals 是内容相等（同证书重建的路径相等），故「不同 certPath」负例需用真正不同的证书；toString 格式是 "(timestamp: …TSA: […]")。
