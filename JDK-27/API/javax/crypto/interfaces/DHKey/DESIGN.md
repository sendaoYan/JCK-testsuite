# javax.crypto.interfaces.DHKey — 测试设计

## 1. API 功能
DH 家族的最小公共接口（JDK 1.4 起），唯一职责：暴露密钥携带的 Diffie-Hellman 域参数。

- `DHParameterSpec getParams()`：返回该密钥的 DH 参数（素数模 p、生成元 g、可选私钥指数长度 L）。
- 无字段（本接口没有 serialVersionUID）；`DHPrivateKey`、`DHPublicKey` 均继承本接口。

## 2. Java 用户如何使用
接口本身不可实例化，实例来自 `KeyPairGenerator.getInstance("DiffieHellman")`：

```java
KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
kpg.initialize(512);
KeyPair kp = kpg.generateKeyPair();
DHKey key = (DHKey) kp.getPrivate();          // 两侧密钥都能当作 DHKey
DHParameterSpec sp = key.getParams();          // p / g / L
BigInteger p = sp.getP(), g = sp.getG();
// 把取出的参数交给新的生成器 / AlgorithmParameters 编解码
kpg.initialize(new DHParameterSpec(p, g));
AlgorithmParameters ap = AlgorithmParameters.getInstance("DH");
ap.init(sp);
DHParameterSpec back = ap.getParameterSpec(DHParameterSpec.class);
```

典型组合链路：`getParams() → p/g 校验 → 同参数重开两组密钥 → KeyAgreement 双方对账`，把 DHKey 作为参数源串起整个 DH 家族。

## 3. 开源代码实际用法调研
Web 搜索在本环境不可用（WebSearch 无结果返回），按规则改用 JDK 权威源码与自带测试（路径均在 `~/git/jdk25u-dev`）：

- `src/java.base/share/classes/javax/crypto/interfaces/DHKey.java`：接口定义与 javadoc（"The interface to a Diffie-Hellman key"）。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/SupportedDHKeys.java`（L120-124 附近）：
  ```java
  DHPrivateKey privateKey = (DHPrivateKey)kp.getPrivate();
  BigInteger p = privateKey.getParams().getP();
  if (p.bitLength() != pSize) throw new Exception("Invalid modulus size ...");
  ```
  官方测试正是用 `getParams().getP()` 校验模数位数，再用 p/g 重新 `kpg.initialize(new DHParameterSpec(p, g))` 生成第二组密钥。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyAgreement2.java`（L120、L149）：
  `((DHPublicKey)aliceKpair.getPublic()).getParams()` 取参数发起协商，随后 `doPhase`/`generateSecret` 完成双方对账——即"参数取自 DHKey"的标准用法。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyAgreement3.java`：序列化场景下同样经由 DHKey 视角访问参数。

## 4. 测试用例设计 + 自动化实现
`DHKeyTest.java`，白盒 10 + 黑盒 14 断言。

白盒（testWhiteBox，保留原断言并补充）：
- public/interface/无父类（原有）；声明方法 `getParams`（原有）；
- 返回类型精确为 `javax.crypto.spec.DHParameterSpec` 且为 abstract；
- 无声明字段（无 serialVersionUID）；
- `DHPrivateKey`/`DHPublicKey` 可赋值给 DHKey（isAssignableFrom）。

黑盒（testBlackBox，场景）：
1. KPG("DiffieHellman") 512 位 → 私钥/公钥均 instanceof DHKey。
2. `getParams()` 非 null；p 正、512 位；g 正且 < p；p 为 probable prime（`isProbablePrime(64)`）。
3. 同一密钥对两侧 `getParams()` 的 p/g 值一致；重复调用值一致。
4. 转换链路：params → `AlgorithmParameters("DH")` → 回读 DHParameterSpec，p/g/L 保持（JDK-27 实测 L=160，见 DHPrivateKey 设计）。
5. 家族组合：用 DHKey 的参数初始化两个 KPG → 4 把密钥均 instanceof DHKey → 双方 `KeyAgreement` 秘密相等且长度 = p.bitLength()/8（512→64B）。
6. 参数生成链路：`AlgorithmParameterGenerator("DH")` 512 → 新参数 → KPG → 密钥 instanceof DHKey 且其 params 的 p 与生成参数相等。

确定性：DH 密钥对本身随机，但只断言关系（类型、位长、双方一致、代数关系），断具体 L/长度这类稳定派生值；APG 每次生成不同 p，只断位长与回传关系。
