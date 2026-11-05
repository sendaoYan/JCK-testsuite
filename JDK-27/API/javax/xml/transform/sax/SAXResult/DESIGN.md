# javax.xml.transform.sax.SAXResult — 设计案

## 1. API 功能
javax.xml.transform.sax.SAXResult 类。常量：FEATURE（共 1 个）。 实例/声明方法：setHandler, getHandler, setLexicalHandler, getLexicalHandler, setSystemId, getSystemId。

## 2. Java 用户如何使用
new SAXResult → setSystemId/setLexicalHandler/getHandler。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.sax.SAXResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXResultTest.testBlackBox()`）覆盖：no-arg + setters。

## 5. 深化（本轮新增）
- 构造矩阵 + FEATURE 精确值。
- 事件序列精确闭环：`[SD, SE:root[null], SE:item[3], CH:third, EE:item, SE:item[1], CH:first, EE:item, SE:item[2], CH:second, EE:item, EE:root, ED]`——startDocument 最前、字符聚块、endDocument 收尾。
