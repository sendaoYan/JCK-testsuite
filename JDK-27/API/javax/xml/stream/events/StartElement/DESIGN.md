# javax.xml.stream.events.StartElement — 设计案

## 1. API 功能
javax.xml.stream.events.StartElement 接口。实例/声明方法：getName, getAttributes, getNamespaces, getAttributeByName, getNamespaceContext, getNamespaceURI。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.StartElement() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.StartElement）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StartElementTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StartElementTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 纯无 ns 属性文档序 a,b,c,d；命名空间元素 QName 三元组 + getNamespaceURI(p)/('')/未知 + NamespaceContext
  五查（URI('')/URI(p)/prefix 反查/prefixes 计数/URI(null)→IAE）。
- 混合属性迭代顺序锁定 a,c,b（无 ns 在前按文档序、有 ns 在后，确定性）。
- 编码输出锁定 `<['http://p']:p:r a='1' c='3' p:b='2' xmlns:='http://d' xmlns:p='http://p'>`（括号形态 + xmlns:= quirk）。
- getAttributes 迭代器 remove→UOE；空迭代器；未声明前缀属性→XMLStreamException(AttributePrefixUnbound)。
- 工厂：无 ns 属性插入序；getNamespaceURI(x)=null/getNamespaceContext()=null（impl quirk）；
  createStartElement(prefix,ns,local) 编码为 `<['http://p']:p:local>`。
