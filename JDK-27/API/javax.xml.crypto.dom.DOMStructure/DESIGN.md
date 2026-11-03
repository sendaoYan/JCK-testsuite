# javax.xml.crypto.dom.DOMStructure — 设计案

## 1. API 功能
javax.xml.crypto.dom.DOMStructure 类。实例/声明方法：getNode, isFeatureSupported。

## 2. Java 用户如何使用
DOMStructure(Node) → getNode() 引用同一节点；isFeatureSupported(未知) 返回 false。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dom.DOMStructure）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMStructureTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMStructureTest.testBlackBox()`）覆盖：构造节点引用、isFeatureSupported 布尔。。
