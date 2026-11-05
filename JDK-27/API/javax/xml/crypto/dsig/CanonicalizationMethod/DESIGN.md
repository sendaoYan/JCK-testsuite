# javax.xml.crypto.dsig.CanonicalizationMethod — 设计案

## 1. API 功能
javax.xml.crypto.dsig.CanonicalizationMethod 接口（javax.xml.crypto 家族）。测试文件：CanonicalizationMethodTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 CanonicalizationMethodTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化，JDK-27 版）
4 个 INCLUSIVE/EXCLUSIVE(±Comments) URI 全表全等；EXCLUSIVE 接受 ExcC14NParameterSpec 恒等往返、INCLUSIVE 拒绝 spec IAE；INCLUSIVE vs EXCLUSIVE 规范化字节差异→摘要差异（弱断言 contains/长度）；PrefixList 改变 EXCLUSIVE 摘要；端到端验证

## 5. JDK-27 独有补充断言
JDK-27 独有常量 INCLUSIVE_11/INCLUSIVE_11_WITH_COMMENTS 全等；两种 1.1 形态构造 + 端到端往返验证；1.1 形态拒绝 spec 参数

## 6. 已知版本差异（JDK-27 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
