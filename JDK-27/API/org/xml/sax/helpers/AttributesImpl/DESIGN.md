# org.xml.sax.helpers.AttributesImpl — 设计案

## 1. API 功能
org.xml.sax.helpers.AttributesImpl 类。实例/声明方法：getLength, getURI, getLocalName, getQName, getType, getValue, getIndex, clear, setAttributes, addAttribute, setAttribute, removeAttribute, setURI, setLocalName, setQName, setType, setValue。

## 2. Java 用户如何使用
addAttribute → get* 系列 → set/remove/clear → copy ctor/setAttributes。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.AttributesImpl）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 AttributesImplTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`AttributesImplTest.testBlackBox()`）覆盖：增查改删清 + 拷贝构造。
