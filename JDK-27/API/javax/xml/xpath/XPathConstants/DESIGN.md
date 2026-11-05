# javax.xml.xpath.XPathConstants — 设计案

## 1. API 功能
javax.xml.xpath.XPathConstants 类。常量：DOM_OBJECT_MODEL（共 1 个）。

## 2. Java 用户如何使用
常量 QName 的 namespaceURI/localPart 精确断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathConstants）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathConstantsTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathConstantsTest.testBlackBox()`）覆盖：6 个常量的精确值与互异性。

## 5. 深化条目（本批新增，XPathConstantsTest 24→43 断言）
- **五 QName 精确契约**：ns 均为 "http://www.w3.org/1999/XSL/Transform"（XSL/XPath 1.0 命名空间，**非** XPath 语言 URI——实测否定任务初稿）；localPart 精确；身份稳定、五者互异、prefix null/空；equals 按 (ns,local)。
- **使用闭环**：每常量驱动对应 evaluate 返回类型；DOM_OBJECT_MODEL 被 newInstance 接受。
