# java.security.interfaces.DSAKeyPairGenerator 黑盒行为测试设计

## 1. API 功能
历史接口：initialize(DSAParams,SecureRandom) 与 initialize(int,boolean,SecureRandom)（modlen/genParams 形态）。

## 2. Java 用户如何使用
JDK 1.1 时代接口；JDK-27 的 KeyPairGenerator.getInstance("DSA") 返回委托包装，不再实现该接口（实测）。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk 无直接实现用例；现代等价路径 initialize(AlgorithmParameterSpec)。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：自定义 Impl 记录两种 initialize 的参数与 SecureRandom 引用并验证记录行为 + 初始态；断言现代 generator 不实现本接口（记录现状）；DSAParameterSpec 初始化路径产出 DSAPublicKey。

断言入口：`DSAKeyPairGeneratorTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
