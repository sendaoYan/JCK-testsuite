# java.security.interfaces.RSAMultiPrimePrivateCrtKey 黑盒行为测试设计

## 1. API 功能
多素数 CRT 私钥接口：CRT 六字段 + getOtherPrimeInfo() + serialVersionUID 618058533534628008L；JDK-27 中 extends RSAPrivateKey（与 RSAPrivateCrtKey 兄弟，源码实测）。

## 2. Java 用户如何使用
PKCS#1 v2.2 多素数形态；JDK 不生成此类密钥（实测）。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/KeyFactory/GenerateRSAPrivateCrtKey.java（对照：普通 CRT）。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：自定义 Impl（RSAOtherPrimeInfo 定值）验证全 getter 契约与防御性克隆；instanceof RSAPrivateKey；生成的 RSA 私钥不是本类型；RSAMultiPrimePrivateCrtKeySpec 经 KeyFactory 转换后多素数信息被丢弃（得到普通 RSAPrivateKey，实测记录）。

断言入口：`RSAMultiPrimePrivateCrtKeyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化补遗（testContract）
记录型最小实现（getter 恒等/null 透传）+ 继承格 isAssignableFrom 全链 + 无自身 serialVersionUID 接口逐一验证 + spec/序列化 round-trip 升级为全等（equals）断言 + equals/hashCode 语义（自反、公私不等、稳定）。DSA 侧补 g^q mod p == 1、y == g^x mod p、p/q 素性；RSA 侧补 d·e ≡ 1 (mod lcm(p-1,q-1))（λ 而非 φ）、RSAKeyGenParameterSpec 转向、(n,d) 最小规格还原丢 CRT；EC 侧补 W 在曲线方程上（y² = x³+ax+b mod p）。JDK-27 特有差异：DSAParams extends AlgorithmParameterSpec；DSAKeyPairGenerator 的 throws 子句已删除。
