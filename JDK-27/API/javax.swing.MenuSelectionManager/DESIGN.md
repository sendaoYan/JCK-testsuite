# javax.swing.MenuSelectionManager 设计案

## 1. API 功能
`MenuSelectionManager` 管理菜单选择路径。方法分组：
- 路径：`setSelectedPath/getSelectedPath/clearSelectedPath`。
- 监听：`addChangeListener/removeChangeListener/getChangeListeners`。
- 查询：`isComponentPartOfCurrentMenu/componentForPoint`。

## 2. Java 用户如何使用
用户获取默认管理器管理菜单选择：
```java
MenuSelectionManager m = MenuSelectionManager.defaultManager();
m.setSelectedPath(new MenuElement[] { menuBar, menu, item });
```
典型链路：`defaultManager()` → `setSelectedPath/getSelectedPath/clearSelectedPath`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/MenuSelectionManager.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/MenuSelectionManager.java`）。
- `BasicPopupMenuUI` 使用它处理菜单弹出与选中。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验普通类。
- 行为（黑盒）：`testBlackBox()` 覆盖 `defaultManager` 单例、初始空路径、`setSelectedPath(null)/clearSelectedPath`、`isComponentPartOfCurrentMenu`、监听器 add/remove。
