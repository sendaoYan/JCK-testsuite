# javax.swing.DefaultFocusManager 设计案

## 1. API 功能
`DefaultFocusManager` 是 `FocusManager` 的默认子类，提供 Swing 焦点遍历。方法分组：
- 遍历：`getComponentAfter/getComponentBefore/getFirstComponent/getLastComponent`。
- 比较：`compareTabOrder(Component, Component)`。

## 2. Java 用户如何使用
用户一般通过 `FocusManager.getCurrentManager()` 获得，或自定义 `FocusManager` 子类控制 Tab 顺序：
```java
FocusManager fm = FocusManager.getCurrentManager();
boolean before = ((DefaultFocusManager) fm).compareTabOrder(a, b);
```
典型链路：`new DefaultFocusManager` → `compareTabOrder` → `getComponentAfter/getFirstComponent`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/FocusManager.java` 的默认实现即 `DefaultFocusManager`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/FocusManager.java`）。
- JDK 源码 `javax/swing/DefaultKeyboardFocusManager.java` 被 `FocusManager` 继承。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `FocusManager`、无接口。
- 行为（黑盒）：`testBlackBox()` 覆盖 `compareTabOrder` 的 z-order 比较，以及非 displayable 容器下 `getFirstComponent/getLastComponent` 返回 null。
