# javax.xml.crypto.dsig.keyinfo.KeyInfoFactory — 设计案

## 1. API 功能
javax.xml.crypto.dsig.keyinfo.KeyInfoFactory 抽象类（javax.xml.crypto 家族）。测试文件：KeyInfoFactoryTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 KeyInfoFactoryTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化）
工厂矩阵（默认/DOM/Provider/名称 + Bogus→NSME 含机制名、null NPE、未知 Provider）；全 new* 方法（KeyName/KeyValue RSA-DSA-EC 等价 + DH→KeyException 含算法名/PGPData 8 字节与 0xC5 tag 规则/X509IssuerSerial 精确消息/X509Data CCE-IAE-NPE/RetrievalMethod 最小形/KeyInfo 组装与不可变）；unmarshalKeyInfo 往返 + 非 KeyInfo 元素 MarshalException

## 5. 已知版本差异（JDK-8 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
