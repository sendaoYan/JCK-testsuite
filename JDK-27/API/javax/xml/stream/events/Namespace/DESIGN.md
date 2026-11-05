# javax.xml.stream.events.Namespace — 设计案

## 1. API 功能
javax.xml.stream.events.Namespace 接口。实例/声明方法：getPrefix, getNamespaceURI, isDefaultNamespaceDeclaration。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.Namespace() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.Namespace）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NamespaceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NamespaceTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 矩阵：默认声明 prefix=""/isDefault=T/getName={xmlns-ns}""；前缀声明 local=前缀/isDefault=F；
  getValue=URI、getDTDType="CDATA"、isSpecified=F、isNamespace=T/isAttribute=F（覆写）、码 13。
- createNamespace("",uri) 显式空前缀→默认声明；单参 null 容忍；双参 null 前缀→IAE。
- 解析：`<r xmlns='http://d' xmlns:p='http://p'/>` 两声明逐字段精确 + 父 SE.getNamespaceURI(p)。
- 闭环：工厂 SE 含前缀 ns → 编码含 `xmlns:p='http://p'`。
