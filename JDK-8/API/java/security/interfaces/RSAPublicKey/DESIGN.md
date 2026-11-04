# java.security.interfaces.RSAPublicKey — 设计案

## 1. API 功能
RSA 公钥接口：getPublicExponent()，继承 RSAKey 与 PublicKey。serialVersionUID=-8727434096241101194L。

## 2. 使用方式
生成后读 e 与 n；Signature.initVerify 消费；KeyFactory 双规格还原。

## 3. 参考来源
- javadoc `java.security.interfaces.RSAPublicKey`。
- SunRsaSign provider 文档（默认指数 65537）。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：e/模数/算法/格式/编码透传。
- 继承格：RSAKey + PublicKey → Key、Serializable。
- 真实闭环（RSA-2048）：**e == 65537（SunRsaSign 固定值，确定性）**、e ∈ (0, n) 且为奇数；X.509 格式；RSAPublicKeySpec 与 X509EncodedKeySpec round-trip equals；序列化 round-trip equals；SHA256withRSA 验签成功/异消息失败。
- equals/hashCode：自反、公私不等、稳定。
- 易错点：e=65537 是 SunRsaSign 实现行为（可断言），但 RSAKeyGenParameterSpec 显式指定其它指数时以指定值为准。
