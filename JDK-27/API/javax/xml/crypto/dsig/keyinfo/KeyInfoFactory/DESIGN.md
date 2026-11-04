# javax.xml.crypto.dsig.keyinfo.KeyInfoFactory — 设计案

## 1. API 功能
javax.xml.crypto.dsig.keyinfo.KeyInfoFactory 抽象类。静态入口：getInstance。 实例/声明方法：getMechanismType, getProvider, newKeyInfo, newKeyName, newKeyValue, newPGPData, newRetrievalMethod, newX509Data, newX509IssuerSerial, isFeatureSupported, getURIDereferencer, unmarshalKeyInfo。

## 2. Java 用户如何使用
getInstance() → 各 new* 方法返回真实 DOM 对象并断言其 getter 往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.keyinfo.KeyInfoFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 KeyInfoFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`KeyInfoFactoryTest.testBlackBox()`）覆盖：机制类型 DOM、provider、各对象 getter 往返、isFeatureSupported、默认 URIDereferencer。。
