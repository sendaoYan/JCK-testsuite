# javax.xml.stream.events.Namespace — 设计案

## 1. API 功能
命名空间声明事件接口（extends Attribute）。方法：getPrefix、getNamespaceURI、isDefaultNamespaceDeclaration
（继承 getName/getValue/getDTDType/isSpecified）。事件码 NAMESPACE=13。

## 2. Java 用户如何使用
工厂 createNamespace(uri)（默认声明）与 createNamespace(prefix, uri)（前缀声明）；解析：作为
StartElement.getNamespaces() 迭代项出现（不产生独立事件）。

## 3. 开源代码实际用法调研
JDK 实现 NamespaceEvent；JDK test/jdk javax/xml/stream；本仓库 NamespaceTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 矩阵：createNamespace(uri) → prefix=""/isDefault=T/getName={http://www.w3.org/2000/xmlns/}""（local 为空串）；
  createNamespace(prefix,uri) → prefix 保留/isDefault=F/getName local=前缀；两形态 getValue()==URI、
  getDTDType()=="CDATA"、isSpecified()=F、isNamespace()=T 且 isAttribute()=F（子接口覆写）、事件码 13。
- createNamespace("",uri) 显式空前缀 → 默认声明；createNamespace(null) 单参容忍为默认声明；
  createNamespace(null,uri) 双参 → IllegalArgumentException。
- 解析路径：`<r xmlns='http://d' xmlns:p='http://p'/>` 的 getNamespaces 两个条目逐字段精确；
  父 StartElement.getNamespaceURI(p) 同步可用。
- 闭环：工厂 SE 带前缀 ns → 编码输出含 `xmlns:p='http://p'`。
