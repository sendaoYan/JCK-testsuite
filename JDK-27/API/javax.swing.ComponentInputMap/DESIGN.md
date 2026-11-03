# javax.swing.ComponentInputMap 设计案

## 1. API 功能
`ComponentInputMap` 是绑定到特定 `JComponent` 的 `InputMap` 子类。方法分组：
- 构造：`ComponentInputMap(JComponent)`。
- 组件：`getComponent()`。
- 映射：`put(KeyStroke, Object)`、`get`、`remove`、`clear`（覆盖父类，额外通知组件清除 UI 映射）。
- 层级：`setParent(InputMap)`。

## 2. Java 用户如何使用
用户为组件绑定按键到动作：
```java
ComponentInputMap map = new ComponentInputMap(component);
map.put(KeyStroke.getKeyStroke("ENTER"), "commit");
component.setInputMap(JComponent.WHEN_FOCUSED, map);
```
典型链路：`new ComponentInputMap(component)` → `put(keystroke, action)` → `get` → `remove/clear`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JComponent.java` 的 `setInputMap` 会创建/使用 `ComponentInputMap`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java`）。
- JDK 源码 `javax/swing/LookAndFeel.java` 的 `makeComponentInputMap` 返回 `ComponentInputMap`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `InputMap`、无接口。
- 行为（黑盒）：`testBlackBox()` 覆盖 `getComponent`、`put/get/size/keys`、`remove`、`clear`。
