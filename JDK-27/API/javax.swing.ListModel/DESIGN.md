# javax.swing.ListModel — DESIGN

## 1. API 功能
泛型接口（`javax.swing.ListModel<E>`），JList 的数据源契约：
- 查询：`getSize()`、`getElementAt(int)`
- 监听：`add/removeListDataListener`
- 参考实现：`javax.swing.DefaultListModel<E>`（Vector 委托 + 事件通知），另含 `addElement/insertElementAt/removeElement(At)/set(ElementAt)/removeAllElements/clear/removeRange/first/lastElement/elements()/toArray/indexOf/lastIndexOf/contains/isEmpty/size/get/set/add/remove/ensureCapacity/capacity/setSize/copyInto/trimToSize/addAll`。

## 2. Java 用户如何使用
- 构造：`new DefaultListModel<>()`，逐个 `addElement` 或循环装入。
- 典型链路（JList 数据维护）：`model.addElement("a")` → `model.insertElementAt("x", 1)` → `model.set(1, "y")` → `model.remove(2)` → `list.setModel(model)`；或 `JList<String> l = new JList<>(model)`。UI 侧实现 `ListDataListener`：`intervalAdded/intervalRemoved/contentsChanged` 各携带 `ListDataEvent(type, index0, index1)`。
- 事件语义（读 jdk25u `DefaultListModel.java` 确认）：add/insert → `INTERVAL_ADDED(i,i)`（append 时 `(n-1,n-1)`）；remove → `INTERVAL_REMOVED(i,i)`；set/setElementAt → `CONTENTS_CHANGED(i,i)`；clear/removeAllElements → `INTERVAL_REMOVED(0, size-1)`；removeRange → `INTERVAL_REMOVED(from,to)`（from>to 抛 IAE）。
- 越界（Vector 语义）：getElementAt/elementAt/removeElementAt/insertElementAt/get/set 越界 → `ArrayIndexOutOfBoundsException`；空表 firstElement/lastElement → `NoSuchElementException`。

## 3. 开源用法调研
WebSearch 在本环境不可靠，改用可验证的 JDK 源码/自带测试并标注来源：
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java`：`setModel(ListModel)` 与 `DefaultListModel` 的全量事件桥接（`ListDataHandler` 处理三种 interval 事件重算选区）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/TestDefListModelException.java`：JDK 自测直接实例化 DefaultListModel 验证异常路径（bug 6187113）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JComboBox/DefaultComboBoxModelAddAllElementsTest.java`：以 ListDataListener 记录 INTERVAL_ADDED 事件断言 index0/index1 —— 与本测试同款黑盒手法。

## 4. 测试用例设计（黑盒部分，`ListModelTest.testBlackBox`）
以接口引用 + 实现引用双视角驱动 `DefaultListModel`，记录型 ListDataListener 全程跟随：
1. 构建：3×addElement（事件类型/索引精确）→ 接口 getSize/getElementAt 顺序。
2. 中位 insert（事件(1,1)、元素移位）；set/setElementAt（CONTENTS_CHANGED、旧值返回）。
3. 查询链：first/last、indexOf/lastIndexOf/indexOf(from)/contains、toArray、elements() 加入序、isEmpty/size。
4. 删除链：removeElement 命中(true+事件(1,1))/未命中(false 无事件)、remove(int) 返回值+事件、removeRange(1,3) 闭区间+事件、removeRange(3,1) IAE。
5. removeAllElements/clear：清空 + INTERVAL_REMOVED(0,1)；事件计数。
6. 容量增长：ensureCapacity(100)、连续 addElement 250 个后值完整、capacity() ≥ size。
7. 异常精确：空表 getElementAt/elementAt(-1)/removeElementAt/insertElementAt(超界)/get/set → AIOOBE；first/lastElement 空表 → NoSuchElementException。
断言合计 46 条。
