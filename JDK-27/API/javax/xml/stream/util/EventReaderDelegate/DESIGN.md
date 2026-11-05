# javax.xml.stream.util.EventReaderDelegate — 设计案

## 1. API 功能
javax.xml.stream.util.EventReaderDelegate 类。实例/声明方法：setParent, getParent, nextEvent, next, hasNext, peek, close, getElementText, nextTag, getProperty, remove。

## 2. Java 用户如何使用
new EventReaderDelegate(base) → getParent/setParent/hasNext。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.util.EventReaderDelegate）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventReaderDelegateTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventReaderDelegateTest.testBlackBox()`）覆盖：父委托往返 + no-arg。

## 5. 契约深化条目（2026-11，testDeep()）
- 覆写 peek 返回 marker 生效（流中多次验证）；透传矩阵：nextEvent/hasNext/next()（需转型）/getElementText
  （文本全等+消费至 END_ELEMENT）/getProperty(命中+未知名 null)/nextTag。
- remove→UOE；构造链：无参 null + setParent/setParent(null)；无父 hasNext/nextEvent/peek/getProperty→NPE，
  **close() 无父也 NPE（透传 close，易漏）**。
- 全流对拍：raw reader 与委托逐事件码相等至双双耗尽。
- getElementText 负例：未消费先调→NPE；在 START_DOCUMENT 上调→XMLStreamException（两条路径分别锁定）。
