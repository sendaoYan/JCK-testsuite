# javax.xml.crypto.dsig.dom.DOMSignContext — 设计案

## 1. API 功能
javax.xml.crypto.dsig.dom.DOMSignContext 类。实例/声明方法：setParent, setNextSibling, getParent, getNextSibling。

## 2. Java 用户如何使用
四个构造器（Key/KeySelector × 2/3 参数）→ getParent/getNextSibling/setParent/setNextSibling。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.dom.DOMSignContext）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMSignContextTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMSignContextTest.testBlackBox()`）覆盖：父/兄弟节点读写、四个构造器字段。。
