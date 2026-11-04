# javax.crypto.spec.DHParameterSpec 设计案

## 1. API 功能
Diffie-Hellman 域参数（p, g 及可选私钥指数位长 l），`public class implements java.security.spec.AlgorithmParameterSpec`：
- 构造：`DHParameterSpec(BigInteger p, BigInteger g)`（l 默认 0）与 `DHParameterSpec(BigInteger p, BigInteger g, int l)`，无校验，BigInteger 直接引用存储。
- 查询：`getP()`/`getG()`/`getL()`。

## 2. Java 用户如何使用
`new DHParameterSpec(p, g)` → `KeyPairGenerator.getInstance("DiffieHellman").initialize(spec)` 生成密钥对 → `KeyAgreement.getInstance("DiffieHellman")` 双方协商；或 `AlgorithmParameters.getInstance("DH").init(spec)` 编码往返。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/DHParameterSpec.java`：两构造器直接赋值，二参构造 `l = 0`。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyFactory.java` L44-49：`DHParameterSpec dhParamSpec = new DHParameterSpec(dhGroup.getPrime(), dhGroup.getBase()); kpgen.initialize(dhParamSpec); ... generateKeyPair();` —— 本测试真实链路同款（固定 p/g + KeyAgreement）。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/SupportedDHKeys.java` L64-66：从生成的 DHPublicKey 取 p/g 再 `new DHParameterSpec(p, g)` 重新初始化。
- 768 位 MODP 素数取自 RFC 2409 group 1（`test/lib/jdk/test/lib/security/DiffieHellmanGroup.java` 同族用法）。

## 4. 测试用例设计 + 自动化实现
`DHParameterSpecTest.java` 白盒（7 check）：public/非 final 非 abstract/extends Object/implements AlgorithmParameterSpec、getP/getG/getL 方法名、两公共构造器。黑盒（16 check）场景：
1. 保留旧断言：p=23/g=5/L=8 精确、二参构造默认 L 0、instanceof。
2. 固定 RFC 2409 768 位 p + g=2：getter 精确、toString(16) 往返、L=96 显式保留。
3. AlgorithmParameters("DH") 编码往返：init(spec) → getParameterSpec(DHParameterSpec.class) → p/g/l 全等。
4. 真实链路：固定 768 位组初始化两个 KeyPairGenerator → 双方公钥 getParams().getP().equals(p) 且 g==2、y 位数在范围内 → KeyAgreement 双方秘密逐字节相等、长度 96、同一密钥对重复协商结果确定。
