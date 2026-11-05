# NamedNodeMap — 设计案

## 1. API 功能
属性节点集合（getLength/item/getNamedItem(NS)/setNamedItem(NS)/removeNamedItem(NS)）。Element.getAttributes() 返回活集合。

## 2. Java 用户如何使用
Element.setAttribute* 后经 getAttributes() 观察；Xerces 缓存同一 map 实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节

## 深化第 2 轮（契约级，2026-11-05）
行为化：两次 getAttributes() 返回同一实例（Xerces 缓存，==）；属性表活视图（setAttribute/removeAttribute 后旧 map 长度同步）；getNamedItem/NS 缺失 null；迭代顺序=插入序；item(pastEnd)=null、item(-1) 抛 IndexOutOfBoundsException（JDK-8 AIOOBE/JDK-27 IOOBE，按父类断言）；setNamedItem 同名返回旧节点且旧节点 ownerElement 置 null、新名返回 null 且追加到尾部；setNamedItemNS/getNamedItemNS 往返；removeNamedItem/NS 缺失 → NOT_FOUND_ERR(8)；setNamedItem(跨 doc attr) → WRONG_DOCUMENT_ERR(4)；Text.getAttributes()==null；克隆 map 独立但计数镜像。
