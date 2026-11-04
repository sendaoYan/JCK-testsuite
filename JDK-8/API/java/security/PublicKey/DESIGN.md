# PublicKey — 设计案

## 1. API 功能
公钥标记接口：extends Key（JDK-8；JDK-27 改 extends AsymmetricKey）；serialVersionUID 7187392471159151072L；自身无方法。

## 2. 使用方式
KeyFactory.generatePublic(X509EncodedKeySpec) 产出；getFormat()=="X.509"；Signature.initVerify 消费；序列化走 KeyRep。

## 3. 参考来源
- javadoc `java.security.PublicKey`。
- 反射实测 sVUID 与继承链。

## 4. 测试用例设计 + 易错规范细节
- 结构：marker（无声明方法）、sVUID 精确、与 PrivateKey 互斥、DSA/RSA 公钥接口是本接口子型、Serializable。
- 真实钥匙闭环：RSA-512 公钥 algorithm/format X.509/编码非空；**KeyFactory 编码 round-trip：generatePublic(X509(encode)) equals 原钥、hashCode 一致、编码相等**（跨实例 Key equals 契约）。
- DSA-1024 公钥 X.509 格式。
- 序列化：KeyRep round-trip equals、格式保留。
- 自定义实现：opaque 钥 null format/encoding 合法。
- 易错点：isAssignableFrom 方向；X509EncodedKeySpec 全名（java.security.spec）。
