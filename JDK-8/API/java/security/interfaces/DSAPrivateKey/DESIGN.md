# java.security.interfaces.DSAPrivateKey — 设计案

## 1. API 功能
DSA 私钥接口：getX() 私钥标量，继承 DSAKey（getParams）与 PrivateKey（Key 三件套）。serialVersionUID=7776497482533790279L。

## 2. 使用方式
KeyPairGenerator 生成后向下转型读取 x；或 KeyFactory 用 DSAPrivateKeySpec 还原/导出。

## 3. 参考来源
- javadoc `java.security.interfaces.DSAPrivateKey`。
- OpenJDK `test/jdk/java/security/KeyFactory/`（DSA 私钥 round-trip 思路）。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值 7776497482533790279L（反射读，防编译期内联干扰）。
- 记录型最小实现：getX/getParams/getAlgorithm/getFormat/getEncoded 全透传。
- 继承格：DSAKey + PrivateKey 双亲，is assignable 到 Key、Serializable；直接父接口恰 2 个。
- 真实闭环（DSA-1024）：x ∈ (0, q)；格式 PKCS#8、编码非空；KeyFactory getKeySpec 保 x、generatePrivate 还原 equals；序列化（KeyRep writeReplace 路径）round-trip equals 且类型保持。
- equals/hashCode：自反、公私不等、hashCode 稳定。
- 易错点：序列化走 KeyRep，还原对象类名同 sun.security.provider.DSAPrivateKey 且 equals 原钥（值相等而非同一实例）。
