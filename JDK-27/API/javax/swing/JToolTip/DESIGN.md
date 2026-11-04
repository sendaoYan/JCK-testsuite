# javax.swing.JToolTip 设计案

## 1. API 功能
`JToolTip` 是工具提示组件。方法分组：
- 文本：`setTipText/getTipText`。
- 关联组件：`setComponent/getComponent`。
- 外观：`getUIClassID/updateUI`。

## 2. Java 用户如何使用
用户定制工具提示：
```java
JToolTip tip = new JToolTip();
tip.setTipText("hover text");
tip.setComponent(component);
```
典型链路：`setTipText` → `getTipText` → `setComponent/getComponent`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JToolTip.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToolTip.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JComponent` + `Accessible`。
- 行为（黑盒）：`testBlackBox()` 覆盖初始 `getComponent` null、`setTipText/getTipText`、`setComponent/getComponent`、`getUIClassID`。
