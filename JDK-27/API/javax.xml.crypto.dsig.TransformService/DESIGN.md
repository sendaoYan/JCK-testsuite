# javax.xml.crypto.dsig.TransformService — 设计案

## 1. API 功能
javax.xml.crypto.dsig.TransformService 抽象类。静态入口：getInstance。 实例/声明方法：getMechanismType, getAlgorithm, getProvider, init, marshalParams。

## 2. Java 用户如何使用
c14n/base64/enveloped/xpath 四种 getInstance → getAlgorithm/getMechanismType/getProvider；init + marshalParams。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.TransformService）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformServiceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformServiceTest.testBlackBox()`）覆盖：算法名精确、c14n init(null)、xpath init(XPathFilterParameterSpec)、marshalParams 不抛。。
