# javax.swing.InternalFrameFocusTraversalPolicy 设计案

## 1. API 功能
`InternalFrameFocusTraversalPolicy` 是内部窗口焦点遍历策略抽象类，继承 `FocusTraversalPolicy`。方法分组：
- 遍历（继承抽象）：`getComponentAfter/getComponentBefore/getFirstComponent/getLastComponent/getDefaultComponent`。
- 初始组件：`getInitialComponent(JInternalFrame)`。

## 2. Java 用户如何使用
用户继承它定制 JInternalFrame 内部的 Tab 焦点顺序：
```java
InternalFrameFocusTraversalPolicy p = new InternalFrameFocusTraversalPolicy() { ... };
internalFrame.setFocusTraversalPolicy(p);
Component first = p.getInitialComponent(internalFrame);
```
典型链路：`new InternalFrameFocusTraversalPolicy(){...}` → `getInitialComponent`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JInternalFrame.java` 使用 `InternalFrameFocusTraversalPolicy` 作为默认焦点策略（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JInternalFrame.java`）。
- JDK 源码 `javax/swing/SortingFocusTraversalPolicy.java` 继承它实现排序遍历。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、父类 `FocusTraversalPolicy`。
- 行为（黑盒）：`testBlackBox()` 用匿名实现覆盖 5 个抽象遍历方法，并验证空内部窗口 `getInitialComponent` 返回 null。
