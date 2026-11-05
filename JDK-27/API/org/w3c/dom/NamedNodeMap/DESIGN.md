# org.w3c.dom.NamedNodeMap — 设计案

## 1. API 功能
org.w3c.dom.NamedNodeMap 接口。实例/声明方法：getNamedItem, setNamedItem, removeNamedItem, item, getLength, getNamedItemNS, setNamedItemNS, removeNamedItemNS。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.NamedNodeMap() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.NamedNodeMap）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NamedNodeMapTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NamedNodeMapTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。


## 深化第 2 轮（契约级，2026-11-05）
行为化：两次 getAttributes() 返回同一实例（Xerces 缓存，==）；属性表活视图（setAttribute/removeAttribute 后旧 map 长度同步）；getNamedItem/NS 缺失 null；迭代顺序=插入序；item(pastEnd)=null、item(-1) 抛 IndexOutOfBoundsException（JDK-8 AIOOBE/JDK-27 IOOBE，按父类断言）；setNamedItem 同名返回旧节点且旧节点 ownerElement 置 null、新名返回 null 且追加到尾部；setNamedItemNS/getNamedItemNS 往返；removeNamedItem/NS 缺失 → NOT_FOUND_ERR(8)；setNamedItem(跨 doc attr) → WRONG_DOCUMENT_ERR(4)；Text.getAttributes()==null；克隆 map 独立但计数镜像。
