# javax.swing.ActionMap 设计案

## 1. API 功能
`ActionMap` 是 `Object -> Action` 的映射，支持父映射回退。方法分组：
- 增删：`put(Object, Action)`、`remove(Object)`、`clear()`。
- 查询：`get(Object)`、`keys()`、`allKeys()`、`size()`。
- 层级：`setParent(ActionMap)`、`getParent()`。

## 2. Java 用户如何使用
用户把按键动作放进 `ActionMap`，与 `InputMap` 配合实现按键绑定：
```java
ActionMap map = component.getActionMap();
map.put("copy", copyAction);
Action a = map.get("copy");
```
典型链路：`new ActionMap` → `put(key, action)` → `get` → `keys/allKeys` → `setParent` → 父链回退 → `remove/clear`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JComponent.java` 用 `ActionMap` 存储组件动作映射（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 用 `ActionMap` + `InputMap` 定义系统按键绑定。
- JDK 源码 `javax/swing/table/TableCellEditor` 等相关 UI 通过 `ActionMap` 提供编辑动作。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `Object`、实现 `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖空映射 size、`put/get/size/keys/allKeys`、缺失键返回 null、`setParent` 后父链回退与 `allKeys` 合并、`remove`、`clear`。
