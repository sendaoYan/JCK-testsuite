# javax.swing.SortingFocusTraversalPolicy 设计案

## 1. API 功能
`SortingFocusTraversalPolicy` 按比较器排序的焦点遍历策略（继承 `InternalFrameFocusTraversalPolicy`）。方法分组：
- 构造：`SortingFocusTraversalPolicy(Comparator)`。
- 隐式向下遍历：`setImplicitDownCycleTraversal/getImplicitDownCycleTraversal`。
- 遍历：`getFirstComponent/getLastComponent/getDefaultComponent/getComponentAfter/getComponentBefore`。

## 2. Java 用户如何使用
用户提供比较器定制焦点顺序：
```java
SortingFocusTraversalPolicy policy = new SortingFocusTraversalPolicy(comparator);
component.setFocusTraversalPolicy(policy);
```
典型链路：`new SortingFocusTraversalPolicy(cmp)` → `getFirstComponent`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SortingFocusTraversalPolicy.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SortingFocusTraversalPolicy.java`）。
- `LayoutFocusTraversalPolicy` 继承它。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `InternalFrameFocusTraversalPolicy`。
- 行为（黑盒）：`testBlackBox()` 覆盖比较器构造、隐式向下遍历开关、空焦点循环根容器下遍历方法返回 null（headless 组件不可显示导致的可判定降级）。
