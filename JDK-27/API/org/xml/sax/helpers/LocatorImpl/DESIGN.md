# org.xml.sax.helpers.LocatorImpl — 设计案

## 1. API 功能
org.xml.sax.helpers.LocatorImpl 类。实例/声明方法：getPublicId, getSystemId, getLineNumber, getColumnNumber, setPublicId, setSystemId, setLineNumber, setColumnNumber。

## 2. Java 用户如何使用
setters → getters + copy ctor。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.LocatorImpl）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LocatorImplTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LocatorImplTest.testBlackBox()`）覆盖：四字段 + 拷贝。

### 深化轮次2（行为契约）
黑盒：新对象位置 0/0（JDK 实现默认，非文档 -1）；拷贝构造是快照非活视图。
