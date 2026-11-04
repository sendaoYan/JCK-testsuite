# javax.xml.crypto.dsig.spec.RSAPSSParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.RSAPSSParameterSpec 类。实例/声明方法：getPSSParameterSpec。

## 2. Java 用户如何使用
RSAPSSParameterSpec(PSSParameterSpec) → getPSSParameterSpec() 往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.RSAPSSParameterSpec）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 RSAPSSParameterSpecTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`RSAPSSParameterSpecTest.testBlackBox()`）覆盖：内部对象引用 + 摘要算法。。
