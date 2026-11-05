# javax.xml.transform.stax.StAXResult — 设计案

## 1. API 功能
javax.xml.transform.stax.StAXResult 类。常量：FEATURE（共 1 个）。 实例/声明方法：getXMLEventWriter, getXMLStreamWriter, setSystemId, getSystemId。

## 2. Java 用户如何使用
new StAXResult(writer) → getter。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.stax.StAXResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StAXResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StAXResultTest.testBlackBox()`）覆盖：两种 writer 构造 + setSystemId。

## 5. 深化（本轮新增）
- 两种构造 round-trip（getXMLStreamWriter/getXMLEventWriter 按构造形态互斥非空）；setSystemId → UOE。
- 真实闭环：Stream→StAXResult(XMLStreamWriter) 恒等 endsWith(DOC)，须先 flush；XMLEventWriter 路径事件顺序差异用 contains 断言。
