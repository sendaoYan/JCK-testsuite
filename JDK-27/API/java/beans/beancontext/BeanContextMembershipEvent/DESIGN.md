# java.beans.beancontext.BeanContextMembershipEvent — 设计案

## 1. API 功能
成员变更事件（extends BeanContextEvent）。构造器 `(BeanContext, Collection)` / `(BeanContext, Object[])`；size/contains/toArray/iterator。

## 2. Java 用户如何使用
childrenAdded/childrenRemoved 传入该事件，遍历新增/删除的子 Bean。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextMembershipEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends BeanContextEvent。黑盒（`testBlackBox`）：数组/集合构造器及 size/contains/toArray/iterator。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 8 → 58。新增：getSource 身份、迭代器/toArray 身份保持；空子项、equals 语义 contains（null 不命中）、toArray 防御性副本、越界 next → NoSuchElementException、hasNext 幂等、Set 构造器、**Object[] 构造器是 Arrays.asList 活视图**（外部数组改动可见——实测语义，非快照）；负例矩阵（null children → NPE 两种构造器、null source → IAE 两种构造器）；数组构造器 iterator remove → UOE vs ArrayList 构造器 remove 生效；传播字段交互；序列化（source transient → getBeanContext() null、children/propagatedFrom 存活为副本）。
- JDK-27 与 JDK-8 完全一致。
