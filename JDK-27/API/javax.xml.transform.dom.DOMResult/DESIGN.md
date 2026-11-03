# javax.xml.transform.dom.DOMResult — 设计案

## 1. API 功能
javax.xml.transform.dom.DOMResult 类。常量：FEATURE（共 1 个）。 实例/声明方法：setNode, getNode, setNextSibling, getNextSibling, setSystemId, getSystemId。

## 2. Java 用户如何使用
new DOMResult(doc/elem) → getNode/getNextSibling/getSystemId。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.dom.DOMResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMResultTest.testBlackBox()`）覆盖：三种构造 + systemId。
