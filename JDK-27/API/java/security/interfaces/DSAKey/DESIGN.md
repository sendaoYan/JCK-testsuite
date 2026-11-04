# java.security.interfaces.DSAKey 黑盒行为测试设计

## 1. API 功能
DSA 密钥公共接口：getParams() 返回 DSAParams 域参数。

## 2. Java 用户如何使用
用户在公私钥两侧统一读 p/q/g。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/KeyFactory/* 与 DSA 用例。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：公私钥都实现；2048/224 参数集（p/g 范围断言）；两侧参数逐字段相等；取出的 params 经 DSAParameterSpec 复用于 KeyPairGenerator 生成同参数新键。

断言入口：`DSAKeyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
