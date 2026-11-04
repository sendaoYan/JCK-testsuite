# org.w3c.dom.bootstrap.DOMImplementationRegistry — 设计案

## 1. API 功能
org.w3c.dom.bootstrap.DOMImplementationRegistry 类。常量：PROPERTY（共 1 个）。 静态入口：newInstance。 实例/声明方法：getDOMImplementation, getDOMImplementationList, addSource。

## 2. Java 用户如何使用
newInstance → getDOMImplementation(Core/XML 3.0/未知) → getDOMImplementationList。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.bootstrap.DOMImplementationRegistry）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMImplementationRegistryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMImplementationRegistryTest.testBlackBox()`）覆盖：PROPERTY、Core/XML 命中、未知 null、列表。
