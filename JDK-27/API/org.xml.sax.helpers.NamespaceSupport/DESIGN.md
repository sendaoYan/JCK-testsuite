# org.xml.sax.helpers.NamespaceSupport — 设计案

## 1. API 功能
org.xml.sax.helpers.NamespaceSupport 类。常量：XMLNS, NSDECL（共 2 个）。 实例/声明方法：reset, pushContext, popContext, declarePrefix, processName, getURI, getPrefixes, getPrefix, getDeclaredPrefixes, setNamespaceDeclUris, isNamespaceDeclUris。

## 2. Java 用户如何使用
declarePrefix/getURI/getPrefix/pushContext/popContext/processName。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.NamespaceSupport）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NamespaceSupportTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NamespaceSupportTest.testBlackBox()`）覆盖：内置 xml 前缀、声明、遮蔽、pop 恢复、processName 三元拆分。
