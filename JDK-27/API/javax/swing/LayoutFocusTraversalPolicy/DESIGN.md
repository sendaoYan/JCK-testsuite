# javax.swing.LayoutFocusTraversalPolicy 设计案

## 1. API 功能
`LayoutFocusTraversalPolicy` 是排序焦点遍历策略（继承 `SortingFocusTraversalPolicy`），按组件布局位置决定焦点顺序。方法分组：
- 遍历：`getComponentAfter/getComponentBefore/getFirstComponent/getLastComponent/getDefaultComponent`。
- 隐式向下遍历：`setImplicitDownCycleTraversal/getImplicitDownCycleTraversal`。

## 2. Java 用户如何使用
用户把它设为组件的焦点遍历策略：
```java
component.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
component.setFocusCycleRoot(true);
```
典型链路：`new LayoutFocusTraversalPolicy` → `getFirstComponent/getComponentAfter`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/LayoutFocusTraversalPolicy.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/LayoutFocusTraversalPolicy.java`）。
- `DefaultFocusTraversalPolicy` 布局遍历的继承层次在此类之上。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `SortingFocusTraversalPolicy` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `setImplicitDownCycleTraversal/getImplicitDownCycleTraversal`，以及空焦点循环根容器下遍历方法返回 null（headless 组件不可显示导致的可判定降级）。
