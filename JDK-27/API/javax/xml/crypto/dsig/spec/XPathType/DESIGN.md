# javax.xml.crypto.dsig.spec.XPathType — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.XPathType 类（javax.xml.crypto 家族）。测试文件：XPathTypeTest.java。

## 2. Java 用户如何使用
XML 数字签名（XMLDSig）API：工厂（XMLSignatureFactory/KeyInfoFactory "DOM" 机制）构造对象图 → DOMSignContext 签名 → DOMValidateContext 验证；算法以 URI 标识；密钥经 KeySelector/KeyInfo 交付。

## 3. 开源代码实际用法调研
JDK 内建 Santuario 实现（com.sun.org.apache.xml.internal.security / org.jcp.xml.dsig.internal.dom）与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 XPathTypeTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化，JDK-27 版）
表达式/过滤恒等往返；默认空命名空间映射；映射拷入/不可变/空映射保持；三常量 toString intersect/subtract/union 且互异；null 表达式/过滤/映射 NPE；JDK-8/27 相同：Filter 为 typesafe-enum 普通类（非 java.lang.Enum、无私有外构造）

## 5. JDK-27 独有补充断言
（本目录无 JDK-27 独有 API；与 JDK-8 行为一致，差异见第 6 节。）

## 6. 已知版本差异（JDK-27 侧记录）
- JDK-8 X509Data.getContent() 为 raw List；JDK-27 为 List<?>。
- JDK-8 XPathFilterParameterSpec/ExcC14NParameterSpec 对 null 前缀键/元素抛 RuntimeException/CCE；JDK-27 接受并保留。
- JDK-8 NoSuchMechanismException 消息形如 "java.security.NoSuchAlgorithmException: Bogus ..."；JDK-27 为 "Mechanism Bogus not available"。
- JDK-27 默认开启 secure validation（禁 1999 XPath transform 等）、RSA 密钥默认 3072 位；JDK-8 无此默认。
