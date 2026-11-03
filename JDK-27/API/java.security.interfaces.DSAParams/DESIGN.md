# java.security.interfaces.DSAParams 黑盒行为测试设计

## 1. API 功能
域参数接口：getP/getQ/getG，继承 AlgorithmParameterSpec。

## 2. Java 用户如何使用
DSA 参数的载体（也用于 AlgorithmParameters 往返）。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/AlgorithmParameters/*。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：默认 2048/224 与 legacy 1024/160 两套（kpg.initialize(1024)）尺寸断言；正值与范围；instanceof AlgorithmParameterSpec；DSAParameterSpec→AlgorithmParameters.init→getParameterSpec 往返逐字段相等。

断言入口：`DSAParamsTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
