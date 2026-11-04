# java.beans.IndexedPropertyChangeEvent — 设计案

## 1. API 功能
索引属性变更事件（extends PropertyChangeEvent）。构造器多一个 `index`；getIndex()。

## 2. Java 用户如何使用
`new IndexedPropertyChangeEvent(source, "items", old, new, index)` 描述数组/列表某索引元素变更。

## 3. 开源代码实际用法调研
JDK 源码 `IndexedPropertyChangeEvent.java`；`PropertyChangeSupport.fireIndexedPropertyChange` 构造之。

## 4. 测试用例设计 + 自动化实现
白盒：extends PropertyChangeEvent。黑盒（`testBlackBox`）：构造器 index + 继承的 property/old/new/source。

## 5. 深化补充（round A，双侧同源）
JDK-27 侧在原白盒+黑盒基础上追加与 JDK-8 侧相同的深化用例（总计 5→37 断言）：
- testConstructorContract：构造器逐字段 + propagationId 默认 null（不在构造器）+ index 边界（0/MAX_VALUE）+ 装箱 old/new。
- testNegativeIndex：-1 与 Integer.MIN_VALUE 原样接受（无校验）。
- testInheritance：instanceof PropertyChangeEvent/EventObject/Serializable + 继承 propagationId/getSource。
- testSerializationRoundTrip：具体类保留、index/name/old/new/propagationId 存活、**source transient → null**。
- testToString：`index=12`/`index=-5` 子串 + 类名前缀。
- testFireThroughSupport：直接构造经 `firePropertyChange(event)` 投递 —— 同一事件对象（identity）到达全局与命名监听器、index 保留、old==new 非 null 不触发。
