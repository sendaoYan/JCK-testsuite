# java.beans.IndexedPropertyDescriptor — 设计案

## 1. API 功能
索引属性描述符（extends PropertyDescriptor）。getIndexedPropertyType/getIndexedReadMethod/getIndexedWriteMethod。

## 2. Java 用户如何使用
描述数组属性（如 `int[] getItems()` 与 `int getItems(int)`），支持按索引读写。

## 3. 开源代码实际用法调研
JDK 源码 `IndexedPropertyDescriptor.java`；`Introspector` 识别索引属性。

## 4. 测试用例设计 + 自动化实现
白盒：extends PropertyDescriptor。黑盒（`testBlackBox`）：数组属性推导 indexed type/methods 及显式构造器。
