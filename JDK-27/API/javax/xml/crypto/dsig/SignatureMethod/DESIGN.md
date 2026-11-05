# javax.xml.crypto.dsig.SignatureMethod — 设计案

## 1. API 功能
javax.xml.crypto.dsig.SignatureMethod 接口（javax.xml.crypto 家族）。测试文件：SignatureMethodTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 SignatureMethodTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化，JDK-27 版）
JDK-8 三个常量全等；全部 URI 族（rsa-sha256/384/512、hmac-sha256/512、ecdsa-sha256、sha256-rsa-MGF1、sha384/sha224 digest）可实例化；HMACParameterSpec 恒等往返；RSA-SHA256/512 端到端往返；hmac-sha512 默认 64 字节、HMACParameterSpec(512)→64 字节并序列化 HMACOutputLength "512"；低于下限→XMLSignatureException "...not less than 512"

## 5. JDK-27 独有补充断言
JDK-27 独有常量表 30 项全等（DSA_SHA256/RSA_SHA224-512/SHA*_RSA_MGF1/ECDSA_SHA1-512/HMAC_SHA224-512/RSA_PSS/ED25519/ED448/SHA3_*_RSA_MGF1/ECDSA_SHA3_*）；14 个新 URI 族可实例化；RSA-PSS 显式参 sign/validate 往返；HMAC-SHA384/512 签名验证与 48/64 字节输出长度断言

## 6. 已知版本差异（JDK-27 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
