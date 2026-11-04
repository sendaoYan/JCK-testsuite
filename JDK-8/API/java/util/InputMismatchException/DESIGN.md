# java.util.InputMismatchException 测试设计案（JDK-8）

> 覆盖目标：完整契约：构造器、Scanner 真实触发、继承 NoSuchElementException。

## 1. API 功能

- 构造器：无参 / (String)；继承 NoSuchElementException（unchecked）

## 2. 使用方式

new Scanner("abc").nextInt() → InputMismatchException（消息可 null）。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/InputMismatchException`
- JDK 源码 Scanner 类型不匹配抛点

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | Scanner nextInt 遇非数字触发 | testThrownByScanner |
| P0 | 构造器消息读回 + 继承关系 | testHierarchyAndCtor |
| P0 | 契约深化：层次/构造矩阵/Scanner 触发 | testMismatchContracts |
