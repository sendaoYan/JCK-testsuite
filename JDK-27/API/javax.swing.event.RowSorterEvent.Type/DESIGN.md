# RowSorterEvent.Type 行为测试设计

## 1. API 功能
`javax.swing.event.RowSorterEvent$Type` 是行排序器事件类型枚举（`java.lang.Enum` 子类），只有两个常量。

- **常量**：`SORT_ORDER_CHANGED`（ordinal 0）、`SORTED`（ordinal 1）。
- **查询**：继承的 `name()`/`ordinal()`/`values()`/`valueOf(String)`。

## 2. Java 用户如何使用
作为 `RowSorterEvent.getType()` 返回值做身份比较：

```java
if (e.getType() == RowSorterEvent.Type.SORTED) { ... }
else if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) { ... }
```

每个方法典型调用：静态常量身份比较 → 需要枚举遍历时 `RowSorterEvent.Type.values()` → 反序列化时 `valueOf("SORTED")`。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/RowSorter.java:332`：
   ```java
   fireRowSorterChanged(new RowSorterEvent(this, RowSorterEvent.Type.SORTED, lastRowIndexToModel));
   ```
   用 `RowSorterEvent.Type.SORTED` 作为事件类型参数。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（枚举、继承 Enum、public、无接口、两个枚举常量及顺序）。
- 黑盒：验证 `name()`/`ordinal()` 精确值、常量身份互异、`values()` 顺序、`valueOf("SORTED"/"SORT_ORDER_CHANGED")` round-trip。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
