# java.util.NoSuchElementException 测试设计案（JDK-8）

> 覆盖目标：完整契约：构造器（9+ 增加 (String,Throwable)/(Throwable)）、迭代器/枚举/Scanner 真实触发。

## 1. API 功能

- 构造器：无参 / (String)；JDK-9+ (String,Throwable)/(Throwable)（反射探测）

## 2. 使用方式

空迭代器 next()、空枚举 nextElement()、耗尽的 Scanner next() → NSEE。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/NoSuchElementException`
- JDK 9+ 源码新增构造器

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 三来源触发（Iterator/Enumeration/Scanner） | testNseeContracts |
| P0 | 构造器消息读回 | testConstructors |
| P1 | 9+ 双参构造存在性（27 直测、8 反射缺失） | testNseeContracts |
| P0 | 契约深化：触发矩阵/(Throwable) 构造(9+)/空迭代器触发；序列化 round-trip/Vector 首末元素触发/空枚举迭代器双触发 | testNseeContracts |
