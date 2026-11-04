# javax.swing.InputMap 设计案

## 1. API 功能
`InputMap` 是 `KeyStroke -> Object` 映射，支持父映射回退。方法分组：
- 增删：`put(KeyStroke, Object)`、`remove(KeyStroke)`、`clear()`。
- 查询：`get(KeyStroke)`、`keys()`、`allKeys()`、`size()`。
- 层级：`setParent/getParent`。

## 2. Java 用户如何使用
用户把按键映射到动作名，配合 `ActionMap` 使用：
```java
InputMap map = component.getInputMap();
map.put(KeyStroke.getKeyStroke("ENTER"), "commit");
```
典型链路：`new InputMap` → `put(keystroke, action)` → `get` → `keys/allKeys` → `setParent` 回退。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JComponent.java` 使用 `InputMap` 存储组件按键映射（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java`）。
- JDK 源码 `javax/swing/LookAndFeel.java` 的 `makeInputMap` 返回 `InputMap`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖空 size、`put/get/keys/allKeys/size`、缺失键 null、`setParent` 后父链回退与 `allKeys` 合并、`remove/clear`。
