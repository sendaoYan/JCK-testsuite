# java.security.interfaces.XECPublicKey 黑盒行为测试设计

## 1. API 功能
XDH 公钥：getU() BigInteger。

## 2. Java 用户如何使用
KeyAgreement 对端密钥。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/KeyAgreement/KeySpecTest.java（XECPublicKeySpec 用法）。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：u∈[0,2^255)（X448≤448 位）；XECPublicKeySpec(params,u) 重建 u 相等（params 按名比较，实测）；用重建公钥做 KeyAgreement 与原公钥产生相同秘钥。

断言入口：`XECPublicKeyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化补遗（testContract）
记录型最小实现（Optional/EdECPoint/BigInteger getter 透传）+ 继承格 isAssignableFrom 全链（EdEC* 不声明自身 serialVersionUID，继承 PrivateKey 6034044314589513430L / PublicKey 7187392471159151072L——getDeclaredField 验证）+ NamedParameterSpec 恒等语义（钥的 params 是携带同名参数的独立实例，!= 常量）+ 序列化 round-trip equals + equals/hashCode 语义 + XECPublicKey u ∈ [0, 2^255)。
