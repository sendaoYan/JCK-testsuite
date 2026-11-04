# javax.swing.colorchooser.DefaultColorSelectionModel 测试设计（JDK-8 侧）

## 1. API 功能
标准模型：无参默认 WHITE、显式 Color 构造、setSelectedColor 事件化、getChangeListeners、
Serializable。

## 2. Java 用户如何使用
`JColorChooser chooser = new JColorChooser(new DefaultColorSelectionModel(color))`；
模型上挂 ChangeListener 做联动。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/colorchooser/DefaultColorSelectionModel.java`；本仓库 probe（null 语义/序列化）。

## 4. 测试用例设计 + 易错细节（headless 锁定，EDT）
- 默认 WHITE；显式构造保留；事件计数：异色恰好 1 次、同色 0 次、事件 source==模型。
- **setSelectedColor(null)：不抛、颜色不变、0 事件（静默忽略）**；
  **ctor(null) 合法且 getSelectedColor()==null**；
  **null 初始化的模型再 setSelectedColor(颜色) → NPE（JDK-8/JDK-27 行为一致，内部经 null 选择比较）**。
- getChangeListeners 记账（0→1→2→remove→1；移除不存在的监听器无副作用）；
  remove 后不再通知。
- 序列化往返：颜色保留；监听器不序列化（干净模型往返后 getChangeListeners().length==0）；
  带非 Serializable 监听器的模型序列化会 NotSerializableException（避免在断言中踩）。

自动化实现位于同目录 `DefaultColorSelectionModelTest.java`（headless，无 @key）。
