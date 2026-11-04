# java.security.interfaces.DSAPrivateKey 黑盒行为测试设计

## 1. API 功能
DSA 私钥：getX() + serialVersionUID 7776497482533790279L。

## 2. Java 用户如何使用
签名（SHA256withDSA）、KeyFactory 重建、序列化。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/KeyFactory/DSA*。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：serialVersionUID 精确值；x∈[1,q)；PKCS#8 格式；SHA256withDSA 签名可被公钥验证；DSAPrivateKeySpec(x,p,q,g) 重建 x 相等；Java 序列化往返 x 保持。

断言入口：`DSAPrivateKeyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化补遗（testContract）
记录型最小实现（getter 恒等/null 透传）+ 继承格 isAssignableFrom 全链 + 无自身 serialVersionUID 接口逐一验证 + spec/序列化 round-trip 升级为全等（equals）断言 + equals/hashCode 语义（自反、公私不等、稳定）。DSA 侧补 g^q mod p == 1、y == g^x mod p、p/q 素性；RSA 侧补 d·e ≡ 1 (mod lcm(p-1,q-1))（λ 而非 φ）、RSAKeyGenParameterSpec 转向、(n,d) 最小规格还原丢 CRT；EC 侧补 W 在曲线方程上（y² = x³+ax+b mod p）。JDK-27 特有差异：DSAParams extends AlgorithmParameterSpec；DSAKeyPairGenerator 的 throws 子句已删除。
