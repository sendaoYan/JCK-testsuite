# java.util.ConcurrentModificationException 测试设计案（JDK-8）

> 覆盖目标：完整契约：4 个构造器、fail-fast 真实触发（集合迭代中结构性修改）、序列化。

## 1. API 功能

- 构造器：无参 / (String) / (String, Throwable)（JDK-7+）/ (Throwable)（JDK-7+）
- getCause；继承 RuntimeException

## 2. 使用方式

ArrayList 迭代中 add → 抛 CME；直接构造异常断言消息/cause 读回。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/ConcurrentModificationException`
- JDK 源码 ArrayList modCount fail-fast 检查

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 迭代中结构性修改触发 CME | testCmeContracts |
| P0 | 4 构造器消息/cause 读回 | testConstructors |
| P1 | (String,null) 与 (null,Throwable) 边界 | testCmeContracts |
| P0 | 契约深化：构造矩阵/cause 保持/触发（fail-fast 迭代）；序列化 round-trip/initCause 一次性/suppressed/多载体触发 | testCmeContracts |
