# javax.swing.DropMode 设计案

## 1. API 功能
`DropMode` 是拖放目标放置模式枚举，控制 drop 位置语义。常量：`USE_SELECTION`、`ON`、`INSERT`、`INSERT_ROWS`、`INSERT_COLS`、`ON_OR_INSERT`、`ON_OR_INSERT_ROWS`、`ON_OR_INSERT_COLS`。

## 2. Java 用户如何使用
用户设置组件的放置模式：
```java
component.setDropMode(DropMode.INSERT_ROWS);
DropMode mode = component.getDropMode();
```
典型链路：`DropMode.valueOf` → `setDropMode` → `getDropMode` → `name/ordinal`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JList.java`、`JTable.java`、`JTree.java` 的 `setDropMode/getDropMode` 使用 `DropMode`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/`）。
- JDK 测试 `test/jdk/javax/swing/JList/`、`JTable/` 覆盖各 drop 模式。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isEnum、父类 `Enum`、8 个常量名。
- 行为（黑盒）：`testBlackBox()` 覆盖 `values/valueOf/name/ordinal`。
