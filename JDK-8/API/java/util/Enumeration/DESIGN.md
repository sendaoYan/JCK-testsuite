# java.util.Enumeration 测试设计案（JDK-8）

> 覆盖目标：完整契约：接口两方法、Vector.elements 序、自定义实现、空枚举、asIterator（JDK-9+ 存在侧）。

## 1. API 功能

- 接口方法：hasMoreElements()/nextElement()
- JDK-9+ 默认方法 asIterator()（本侧反射探测存在性）

## 2. 使用方式

Vector.elements() 遍历顺序断言；匿名实现固定序列；耗尽 nextElement → NoSuchElementException。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Enumeration`
- JDK 9+ 源码 Enumeration.asIterator

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | hasMoreElements/nextElement 状态机与耗尽 NSEE | testEnumeration |
| P0 | Vector.elements 顺序保持 | testVectorElements |
| P0 | 空枚举 hasMoreElements false | testEmptyEnumeration |
| P1 | asIterator 存在性（27 有/remove UOE；8 反射缺失） | testEnumerationContracts |
| P0 | 契约深化：asIterator（9+，反射探测）/序列契约；自定义枚举探针（含 null 元素）/空枚举/Vector 非 fail-fast/序保持 | testEnumerationContracts |
