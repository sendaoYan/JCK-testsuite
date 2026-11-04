# javax.crypto.spec.DHPrivateKeySpec 设计案

## 1. API 功能
DH 私钥的透明表示（x, p, g），`public class implements java.security.spec.KeySpec`：
- 构造：`DHPrivateKeySpec(BigInteger x, BigInteger p, BigInteger g)`，无校验。
- 查询：`getX()`（私钥值）/`getP()`/`getG()`。

## 2. Java 用户如何使用
两个方向：正向 `KeyFactory.getInstance("DH").generatePrivate(spec)` 把 spec 变成可用的 PrivateKey（用于 KeyAgreement）；反向 `factory.getKeySpec(dhPrivateKey, DHPrivateKeySpec.class)` 从密钥对象提取 x/p/g。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/DHPrivateKeySpec.java`：纯 getter 数据类。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyFactory.java` L58-77：KeyFactory("DH") 与 DH 密钥编解码往返（本测试采用更稳的 getKeySpec(key, class) 反向往返）。
- `src/java.base/share/classes/javax/crypto/interfaces/DHPrivateKey.java`：`getParams()`/`getX()` 与 spec 字段一一对应——实测 `spec.getX().equals(priv.getX())`。

## 4. 测试用例设计 + 自动化实现
`DHPrivateKeySpecTest.java` 白盒（7 check）：public/非 final 非 abstract/extends Object/implements KeySpec、getX/getP/getG 方法名、单一公共构造器 (x,p,g)。黑盒（15 check）场景：
1. 保留旧断言：x=4/p=23/g=5 accessor、instanceof KeySpec、第二实例同值。
2. 大数固定向量：48 hex 位 x + 768 位 p + g=2 全部 getter 精确。
3. 真实链路（反向）：KeyPairGenerator("DiffieHellman")（固定 768 位组）生成密钥 → KeyFactory("DH").getKeySpec(priv, DHPrivateKeySpec.class) → x/p/g 与 DHPrivateKey 的值逐一相等。
4. 真实链路（正向）：generatePrivate(spec) → 结果 instanceof DHPrivateKey、getX 与 spec 相等、params.p 相等 → 用重建私钥与对方公钥做 KeyAgreement，与原私钥协商的秘密逐字节相等（96 字节）。
