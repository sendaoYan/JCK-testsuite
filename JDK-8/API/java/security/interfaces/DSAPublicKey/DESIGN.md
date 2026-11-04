# java.security.interfaces.DSAPublicKey — 设计案

## 1. API 功能
DSA 公钥接口：getY() 公钥值，继承 DSAKey 与 PublicKey。serialVersionUID=1234526332779022332L。

## 2. 使用方式
生成/还原后读 y 与域参数；Signature.initVerify 直接以接口引用消费。

## 3. 参考来源
- javadoc `java.security.interfaces.DSAPublicKey`。
- RFC/教科书 DSA：y = g^x mod p。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：getY/getAlgorithm/getFormat 透传。
- 继承格：DSAKey + PublicKey 双亲 → Key、Serializable。
- 真实闭环（DSA-1024）：y ∈ (0, p)；**y == g^x mod p（连接公私两侧的数学闭环）**；X.509 格式；DSAPublicKeySpec 与 X509EncodedKeySpec 双 round-trip equals；序列化 round-trip equals；SHA256withDSA initVerify 成功 / 异消息失败。
- 易错点：g^q mod p == 1 与 y == g^x mod p 是确定性数学断言，与随机密钥值无关；spec round-trip 的 equals 依赖实现值语义（Sun provider 成立）。
