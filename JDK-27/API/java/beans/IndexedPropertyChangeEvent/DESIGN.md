# java.beans.IndexedPropertyChangeEvent — 设计案

## 1. API 功能
索引属性变更事件（extends PropertyChangeEvent）。构造器多一个 `index`；getIndex()。

## 2. Java 用户如何使用
`new IndexedPropertyChangeEvent(source, "items", old, new, index)` 描述数组/列表某索引元素变更。

## 3. 开源代码实际用法调研
JDK 源码 `IndexedPropertyChangeEvent.java`；`PropertyChangeSupport.fireIndexedPropertyChange` 构造之。

## 4. 测试用例设计 + 自动化实现
白盒：extends PropertyChangeEvent。黑盒（`testBlackBox`）：构造器 index + 继承的 property/old/new/source。
