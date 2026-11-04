# PrivateKey — 设计案

## 1. API 功能
私钥标记接口：extends Key + javax.security.auth.Destroyable（JDK-8）；serialVersionUID 6034044314589513430L；自身无方法。JDK-27 改为 extends AsymmetricKey（继承链分叉记录）。

## 2. 使用方式
KeyPairGenerator/KeyFactory 产物按 PrivateKey 消费（getAlgorithm/getFormat()=="PKCS#8"/getEncoded）；序列化走 KeyRep 路径。

## 3. 参考来源
- javadoc `java.security.PrivateKey`、`Destroyable`。
- 反射实测 serialVersionUID / Destroyable 方法面。

## 4. 测试用例设计 + 易错规范细节
- 结构：无声明方法（marker）、sVUID 反射精确值、extends Key（经 AsymmetricKey 于 27）+ Destroyable、与 PublicKey 互斥、Serializable、DSA/RSA 私钥接口是本接口子型（isAssignableFrom 方向勿反）。
- 真实钥匙闭环：RSA-512 与 DSA-1024 私钥 getAlgorithm/getFormat/getEncoded 非空、公私不相等。
- 序列化：KeyRep 路径 round-trip equals、算法与编码字节保留。
- Destroyable 默认：isDestroyed()==false；destroy() 抛 DestroyFailedException（**JDK-8 方法名是 isDestroyed()，无 destroyed()**）。
- 自定义实现：opaque 钥允许 getFormat()/getEncoded() 返回 null。
- 易错点：isAssignableFrom 方向（PrivateKey.isAssignableFrom(DSAPrivateKey)）；DSA 生成 1024 位（512 位 DSA 在部分构建不可用）。
