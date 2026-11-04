# java.security.interfaces.ECPrivateKey — 设计案

## 1. API 功能
EC 私钥接口：getS() 私钥标量，继承 ECKey 与 PrivateKey。serialVersionUID=-7896394956925609184L。

## 2. 使用方式
生成后读 s 与参数；KeyFactory 用 ECPrivateKeySpec(s, params) 还原。

## 3. 参考来源
- javadoc `java.security.interfaces.ECPrivateKey`。
- OpenJDK `test/jdk/java/security/KeyFactory/` EC 用例思路。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：getS/getAlgorithm/getFormat/getEncoded 透传。
- 继承格：ECKey + PrivateKey → Key、Serializable。
- 真实闭环（secp256r1）：s > 0 且 bitLength ≤ 域大小 256；PKCS#8 格式；ECPrivateKeySpec round-trip equals 且 s 相同；序列化（KeyRep）round-trip equals。
- equals/hashCode：自反、公私不等、稳定。
- 易错点：s 是 [1, n-1] 内随机值——只断言符号与上界；spec 里的 params 与钥内 params 值相等。
