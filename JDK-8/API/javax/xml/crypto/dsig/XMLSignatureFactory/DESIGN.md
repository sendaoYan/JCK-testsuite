# javax.xml.crypto.dsig.XMLSignatureFactory — 设计案

## 1. API 功能
javax.xml.crypto.dsig.XMLSignatureFactory 抽象类（javax.xml.crypto 家族）。测试文件：XMLSignatureFactoryTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 XMLSignatureFactoryTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化）
旗舰闭环：URI 常量 16 项全等；工厂矩阵（四重载/Bogus→NSME 含名/null NPE/NoSuchProvider/isFeatureSupported(null) NPE+cacheReference false/getURIDereferencer/getKeyInfoFactory 桥）；new* 负例矩阵（bogus digest NSA、digest 参数 IAE、sigmethod 参数 IAE 含 rsa-sha256、bogus transform NSA 含 URI、XPATH 无 spec IAE、c14n 参数 IAE、SignedInfo null NPE+empty IAE 含四参重载、newReference null dm NPE、newXMLSignature null SI NPE）；RSA-2048 签名→256 字节值→树结构 [SignedInfo, SignatureValue]→marshal 后 Algorithm 属性==常量→验证 true→篡改 DigestValue→false→新文档内容篡改→引用 false/签名值 true→篡改 SignatureValue→签名值 false/引用 true→bogus unmarshal MarshalException；HMAC 闭环（默认 32 字节、HMACParameterSpec(256)=32 验证、128→异常）；enveloping 形态（#obj1 引用、Object Id/MimeType、篡改对象→false）

## 5. 已知版本差异（JDK-8 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
