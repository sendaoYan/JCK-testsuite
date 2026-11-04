# javax.xml.crypto.dsig.dom.DOMValidateContext — 设计案

## 1. API 功能
javax.xml.crypto.dsig.dom.DOMValidateContext 类。实例/声明方法：setNode, getNode。

## 2. Java 用户如何使用
两个构造器（Key/KeySelector）+ setNode/getNode。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.dom.DOMValidateContext）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMValidateContextTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMValidateContextTest.testBlackBox()`）覆盖：节点读写、两个构造器字段。。
