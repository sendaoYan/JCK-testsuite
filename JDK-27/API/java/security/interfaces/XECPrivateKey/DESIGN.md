# java.security.interfaces.XECPrivateKey 黑盒行为测试设计

## 1. API 功能
XDH 私钥：getScalar() Optional<byte[]>。

## 2. Java 用户如何使用
KeyAgreement 秘钥协商。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/KeyAgreement/KeySpecTest.java（XECPrivateKeySpec 用法）。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：X25519 32 字节 / X448 56 字节标量；XDH KeyAgreement 32 字节共享秘钥；XECPrivateKeySpec(params,scalar) 重建字节一致（参数在前，实测签名）。

断言入口：`XECPrivateKeyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
