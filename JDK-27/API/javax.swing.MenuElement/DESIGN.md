# javax.swing.MenuElement 设计案

## 1. API 功能
`MenuElement` 接口描述菜单体系节点。方法分组：
- 事件：`processMouseEvent/processKeyEvent/menuSelectionChanged`。
- 结构：`getSubElements/getComponent`。

## 2. Java 用户如何使用
`JMenuBar`/`JMenu`/`JMenuItem` 都实现该接口，构成菜单树：
```java
MenuElement[] path = menuBar.getSubElements();
menuElement.processKeyEvent(keyEvent, path, MenuSelectionManager.defaultManager());
```
典型链路：`getSubElements` → `getComponent` → `menuSelectionChanged`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JMenu.java` 实现 `MenuElement`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenu.java`）。
- `MenuSelectionManager` 遍历 `MenuElement[]` 处理菜单选择路径。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 匿名实现覆盖 `getComponent/getSubElements/menuSelectionChanged`，验证事件方法不抛异常。
