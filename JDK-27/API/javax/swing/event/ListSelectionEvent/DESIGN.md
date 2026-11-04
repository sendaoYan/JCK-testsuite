# ListSelectionEvent 行为测试设计

## 1. API 功能
`javax.swing.event.ListSelectionEvent` 是列表选择事件，继承 `java.util.EventObject`。

- **构造器**：`ListSelectionEvent(Object source, int firstIndex, int lastIndex, boolean isAdjusting)`（注意：不归一化索引）。
- **查询**：`getFirstIndex()`、`getLastIndex()`、`getValueIsAdjusting()`、`toString()`（内嵌 source/firstIndex/lastIndex/isAdjusting）。

## 2. Java 用户如何使用
`ListSelectionModel` 派发，用户实现 `ListSelectionListener`：

```java
list.getSelectionModel().addListSelectionListener(e -> {
    if (e.getValueIsAdjusting()) return;     // 仍在连续调整中
    int first = e.getFirstIndex();
    int last = e.getLastIndex();
    for (int i = first; i <= last; i++) { ... }
});
```

每个方法典型调用：`getValueIsAdjusting()` 判断是否处于连续调整 → `getFirstIndex()/getLastIndex()` 遍历选中区间 → `toString()` 调试。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/DefaultListSelectionModel/4177723/bug4177723.java` 等用例注册 `ListSelectionListener` 读取 `getFirstIndex/getLastIndex/getValueIsAdjusting` 验证选择模型。
2. `jdk25u-dev/src/jdk.hotspot.agent/.../SAListener.java` 等内部工具同样消费 `ListSelectionEvent` 的区间查询。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：构造 isAdjusting=true 事件验证各 getter round-trip；验证 false 情形；验证单点区间（0,0）；关键差异——用 firstIndex=8/lastIndex=2 验证**不**归一化（与 ListDataEvent 对照）；断言 `toString()` 内嵌索引与调整标志。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
