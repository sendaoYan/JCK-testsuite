# javax.swing.undo.StateEditable 设计案

## 1. API 功能
可编辑对象的状态快照接口，供 `StateEdit` 在 `Hashtable` 中保存/恢复状态。

- **常量**：`String RCSID`（历史版本标识）。
- **方法**：`void storeState(Hashtable<Object,Object> state)`（把当前状态写入表）、`void restoreState(Hashtable<?,?> state)`（从表恢复状态）。

## 2. Java 用户如何使用
让业务对象实现该接口，`storeState` 写键值、`restoreState` 读键值：

```java
class Model implements StateEditable {
    String value;
    public void storeState(Hashtable<Object,Object> s) { s.put("value", value); }
    public void restoreState(Hashtable<?,?> s) { value = (String) s.get("value"); }
}
```

典型组合链：`storeState(表) → 修改对象 → restoreState(表)` 完成一次往返，`StateEdit` 内部即按此驱动 undo/redo。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/StateEdit.java`：`init()` 中 `this.object.storeState(this.preState)`、`end()` 中 `this.object.storeState(this.postState)`、`undo()/redo()` 中 `this.object.restoreState(...)` —— 是接口唯一的 JDK 内部消费方。
- `test/jdk/javax/swing/undo/UndoManager/bug4706533.java` 等测试未直接实现该接口，接口作为 SPI 由应用层实现。

## 4. 测试用例设计 + 自动化实现
`StateEditableTest.testBlackBox()` 覆盖：
- 实现类 `storeState` 捕获当前状态、`restoreState` 恢复快照。
- 通过 `StateEditable` 接口引用完成 `store → mutate → restore` 往返。
- 白盒保留结构断言（接口、无父类、恰 2 个方法 `storeState`/`restoreState`）。
