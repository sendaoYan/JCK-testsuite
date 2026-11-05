# javax.xml.crypto.NodeSetData — 设计案

## 1. API 功能
javax.xml.crypto.NodeSetData 接口（javax.xml.crypto 家族）。测试文件：NodeSetDataTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 NodeSetDataTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化）
接口 extends Data + iterator；自定义实现保持顺序与内容、空集迭代零次；cacheReference 下真实签名的 getDereferencedData 为 NodeSetData 且非空（同文档引用）

## 5. 已知版本差异（JDK-8 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
