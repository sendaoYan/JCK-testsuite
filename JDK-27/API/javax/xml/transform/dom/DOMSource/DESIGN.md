# javax.xml.transform.dom.DOMSource — 设计案

## 1. API 功能
javax.xml.transform.dom.DOMSource 类。常量：FEATURE（共 1 个）。 实例/声明方法：setNode, getNode, setSystemId, getSystemId, isEmpty。

## 2. Java 用户如何使用
new DOMSource(node[,systemId]) → getNode/getSystemId。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.dom.DOMSource）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMSourceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMSourceTest.testBlackBox()`）覆盖：三种构造 + setSystemId。

## 5. 深化（本轮新增）
- 构造矩阵 round-trip + FEATURE 精确值 + 无参 setNode/setSystemId。
- 真实闭环：DOM→StreamResult 恒等输出与 DOC 逐字节相等（omit-decl 下）；子树 Element 作 node 亦成立。
- JDK-27 版本面：isEmpty() 覆写——null node 或 newDocument() → true；已解析树 → false；setNode(null) 后 → true（JDK-8 无此方法）。
