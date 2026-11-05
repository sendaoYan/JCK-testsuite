# javax.xml.crypto.dsig.DigestMethod — 设计案

## 1. API 功能
javax.xml.crypto.dsig.DigestMethod 接口（javax.xml.crypto 家族）。测试文件：DigestMethodTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 DigestMethodTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化）
SHA1/SHA256/SHA512/RIPEMD160 常量全等 + sha384/sha224 RFC4051 URI 可实例化；getParameterSpec null；端到端摘要长度 20/32/48/64/28 字节精确断言；负例（bogus URI NSA 消息含 URI、参数 IAE）

## 5. 已知版本差异（JDK-8 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
