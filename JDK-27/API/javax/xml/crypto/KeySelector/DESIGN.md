# javax.xml.crypto.KeySelector — 设计案

## 1. API 功能
javax.xml.crypto.KeySelector 抽象类（javax.xml.crypto 家族）。测试文件：KeySelectorTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 KeySelectorTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化，JDK-27 版）
抽象类结构；singletonKeySelector(null) NPE、固定 key 原样返回且忽略 purpose；回调参数矩阵（RecordingSelector 在真实验证中记录：unmarshal 后的 KeyInfo 内容、purpose==VERIFY、AlgorithmMethod URI==签名方法、context 恒等、恰调用一次）；selector 返回 null→XMLSignatureException "the keyselector did not find a validation key"；selector 抛 KeySelectorException→包装为 "cannot find validation key" 且 cause 恒等保持

## 5. JDK-27 独有补充断言
（本目录无 JDK-27 独有 API；与 JDK-8 行为一致，差异见第 6 节。）

## 6. 已知版本差异（JDK-27 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
