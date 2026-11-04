# javax.xml.crypto.dsig.XMLSignatureFactory — 设计案

## 1. API 功能
javax.xml.crypto.dsig.XMLSignatureFactory 抽象类。静态入口：getInstance。 实例/声明方法：getMechanismType, getProvider, newXMLSignature, newReference, newSignedInfo, newXMLObject, newManifest, newSignatureProperty, newSignatureProperties, newDigestMethod, newSignatureMethod, newTransform, newCanonicalizationMethod, getKeyInfoFactory, unmarshalXMLSignature, isFeatureSupported, getURIDereferencer。

## 2. Java 用户如何使用
getInstance() → 各 new* 方法返回真实 DOM 对象并断言 getter/引用往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.XMLSignatureFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLSignatureFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLSignatureFactoryTest.testBlackBox()`）覆盖：机制类型 DOM、各工厂对象 getter 往返、isFeatureSupported、默认 URIDereferencer。。
