# java.security.interfaces.ECPublicKey 黑盒行为测试设计

## 1. API 功能
EC 公钥：getW() 返回仿射点 + serialVersionUID -3314988629879632826L。

## 2. Java 用户如何使用
验签、点重建。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/KeyFactory/EC*。

## 4. 测试用例设计 + 自动化实现
真实密钥驱动（每类运行时生成，无硬编码密钥材料）：DSA 2048/224（默认与 1024/160 legacy 两套参数）、EC secp256r1/secp384r1、Ed25519/Ed448、X25519/X448、RSA 2048/3072；所有断言为数学性质/往返/签名验证，与具体随机密钥值无关，故确定性成立。 黑盒：serialVersionUID；x/y∈[0,p)；ECPublicKeySpec(w,params) 重建点相等；工厂不校验曲线从属（任意点被接受，实测记录）；序列化往返 W 保持。

断言入口：`ECPublicKeyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
