# ListDataEvent 行为测试设计

## 1. API 功能
`javax.swing.event.ListDataEvent` 是列表数据变更事件，继承 `java.util.EventObject`。

- **常量**：`CONTENTS_CHANGED = 0`、`INTERVAL_ADDED = 1`、`INTERVAL_REMOVED = 2`。
- **构造器**：`ListDataEvent(Object source, int type, int index0, int index1)`，构造时把 `index0/index1` 归一化为升序（`min/max`）。
- **查询**：`getType()`、`getIndex0()`、`getIndex1()`、`toString()`（`"<class>[type=...,index0=...,index1=...]"`）。

## 2. Java 用户如何使用
`ListModel` 实现派发，用户实现 `ListDataListener`：

```java
model.addListDataListener(new ListDataListener() {
    public void intervalAdded(ListDataEvent e) {
        int i0 = e.getIndex0(), i1 = e.getIndex1();  // 已升序
        int type = e.getType();                       // INTERVAL_ADDED
    }
    public void intervalRemoved(ListDataEvent e) { }
    public void contentsChanged(ListDataEvent e) { }
});
```

每个方法典型调用：`getType()` 判断变化类别 → `getIndex0()/getIndex1()` 读取区间（无需再 min/max，构造器已保证 `index0 <= index1`）→ `toString()` 用于调试。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/AbstractListModel.java:125/155/186`：
   ```java
   e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
   e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
   e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
   ```
   三个常量分别对应三类通知方法。
2. `test/jdk/javax/swing/JComboBox/DefaultComboBoxModelAddAllElementsTest.java` 等用例通过 `ListDataListener` 消费该事件验证模型行为。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：断言三个常量值；构造 INTERVAL_ADDED 事件验证 `getType/getIndex0/getIndex1` round-trip；用逆序 index0=20/index1=5 验证构造器归一化；断言 `toString()` 内嵌类型与索引。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
