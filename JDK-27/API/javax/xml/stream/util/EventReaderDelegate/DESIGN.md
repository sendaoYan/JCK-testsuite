# javax.xml.stream.util.EventReaderDelegate — 设计案

## 1. API 功能
javax.xml.stream.util.EventReaderDelegate 类。实例/声明方法：setParent, getParent, nextEvent, next, hasNext, peek, close, getElementText, nextTag, getProperty, remove。

## 2. Java 用户如何使用
new EventReaderDelegate(base) → getParent/setParent/hasNext。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.util.EventReaderDelegate）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventReaderDelegateTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventReaderDelegateTest.testBlackBox()`）覆盖：父委托往返 + no-arg。
