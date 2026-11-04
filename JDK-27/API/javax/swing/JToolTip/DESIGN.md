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


## 5. 深化部分（`JToolTipTest.testBlackBox` 重写，空安全往返 + UI 绑定 + 属性分工）
1. 初始 tipText/component 均 null；setTipText 覆写/置 null、setComponent 换绑/置 null 全部空安全。
2. UI 绑定（Metal 先行）：MetalToolTipUI / getUIClassID=="ToolTipUI" / updateUI 重绑无异常。
3. accessibleContext 非空且实例稳定（两次获取同一对象）。
4. 属性分工：setToolTipText 是 JComponent 级属性，不馈入 getTipText（getTipText 仍 null），两者互不相干。
