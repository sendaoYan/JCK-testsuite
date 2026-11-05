# javax.xml.crypto.dsig.spec.TransformParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.TransformParameterSpec 接口（javax.xml.crypto 家族）。测试文件：TransformParameterSpecTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 TransformParameterSpecTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化，JDK-27 版）
marker 接口（extends AlgorithmParameterSpec）；XPath/XPath2/XSLT/ExcC14N 四实现归属；XPATH 恒等往返；匿名实现对 ENVELOPED→IAE；null 参数对 ENVELOPED 合法

## 5. JDK-27 独有补充断言
（本目录无 JDK-27 独有 API；与 JDK-8 行为一致，差异见第 6 节。）

## 6. 已知版本差异（JDK-27 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
