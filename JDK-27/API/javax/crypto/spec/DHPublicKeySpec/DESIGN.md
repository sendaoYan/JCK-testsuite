# javax.crypto.spec.DHPublicKeySpec 设计案

## 1. API 功能
DH 公钥的透明表示（y, p, g），`public class implements java.security.spec.KeySpec`：
- 构造：`DHPublicKeySpec(BigInteger y, BigInteger p, BigInteger g)`，无校验。
- 查询：`getY()`（公钥值 y = g^x mod p）/`getP()`/`getG()`。

## 2. Java 用户如何使用
正向 `KeyFactory.getInstance("DH").generatePublic(spec)` 得可用 PublicKey（作为 KeyAgreement.doPhase 输入）；反向 `factory.getKeySpec(dhPublicKey, DHPublicKeySpec.class)` 提取 y/p/g。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/DHPublicKeySpec.java`：纯 getter 数据类。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyFactory.java`：X509 编码 ↔ KeyFactory 往返（本测试采用 getKeySpec(key, class) 类型化往返，避免裸编码断言）。
- `src/java.base/share/classes/javax/crypto/interfaces/DHPublicKey.java`：`getY()`/`getParams()` 与 spec 字段对应——实测 `spec.getY().equals(pub.getY())`。

## 4. 测试用例设计 + 自动化实现
`DHPublicKeySpecTest.java` 白盒（7 check）：public/非 final 非 abstract/extends Object/implements KeySpec、getY/getP/getG 方法名、单一公共构造器 (y,p,g)。黑盒（14 check）场景：
1. 保留旧断言：y=4/p=23/g=5 accessor、instanceof KeySpec、第二实例同值。
2. 大数固定向量：getter 精确。
3. 真实链路（反向）：固定 768 位组生成密钥 → getKeySpec(pub, DHPublicKeySpec.class) → y/p/g 与 DHPublicKey 值逐一相等。
4. 真实链路（正向）：generatePublic(spec) → instanceof DHPublicKey、getY 相等、params.p 相等 → 重建公钥参与 KeyAgreement：与原生公钥协商出的双方秘密逐字节相等（96 字节），证明 spec 重建的公钥功能等价。
