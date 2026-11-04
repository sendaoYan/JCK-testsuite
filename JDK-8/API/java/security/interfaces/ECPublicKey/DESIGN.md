# java.security.interfaces.ECPublicKey — 设计案

## 1. API 功能
EC 公钥接口：getW() 返回仿射点 ECPoint(x,y)，继承 ECKey 与 PublicKey。serialVersionUID=-3314988629879632826L。

## 2. 使用方式
生成后读 W 与曲线；Signature.initVerify 消费；KeyFactory 用 ECPublicKeySpec/X509EncodedKeySpec 还原。

## 3. 参考来源
- javadoc `java.security.interfaces.ECPublicKey`。
- SEC 1/SEC 2：仿射点满足曲线方程 y² = x³ + ax + b (mod p)。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：getW 的 x/y 透传。
- 继承格：ECKey + PublicKey → Key、Serializable。
- 真实闭环（secp256r1）：W 仿射坐标 x,y ∈ [0, p)；**在曲线上（y² = x³+ax+b mod p，确定性数学断言）**；X.509 格式；ECPublicKeySpec 与 X509EncodedKeySpec round-trip equals；序列化 round-trip equals；SHA256withECDSA 验签成功/异消息失败。
- 易错点：场素 p 从 ((ECFieldFp) curve.getField()).getP() 取；ECPoint.equals 值语义可直接比较。
