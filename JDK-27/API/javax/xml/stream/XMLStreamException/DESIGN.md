# javax.xml.stream.XMLStreamException — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamException 类。实例/声明方法：getNestedException, getLocation。

## 2. Java 用户如何使用
四种构造 → getNestedException/getLocation/getCause。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamExceptionTest.testBlackBox()`）覆盖：四种构造 + nested exception。
## 5. 契约深化（本轮追加：构造链/装饰/解析器实测）
- 5 构造重载 + getLocation/getNestedException 同一性；消息 "ParseError at [row,col]:[l,c]\nMessage: 原文" 装饰锁定。
- **JDK-8 vs JDK-27 差异**：`new XMLStreamException(String,Location,Throwable).getCause()`——JDK-27 为该 Throwable（JDK-8 为 null，仅 getNestedException 可见）。JDK-27 侧断言 cause==throwable。
- 解析器格式错实测：携带 Location 行号=1。
