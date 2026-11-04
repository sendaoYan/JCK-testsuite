# java.awt.Adjustable 黑盒行为测试设计

## 1. API 功能
- 接口 + 常量：`HORIZONTAL=0`、`VERTICAL=1`、`NO_ORIENTATION=2`。
- 方法：`getOrientation`、`set/getMinimum`、`set/getMaximum`、`set/getUnitIncrement`、`set/getBlockIncrement`、`set/getVisibleAmount`、`set/getValue`、`add/removeAdjustmentListener`。

## 2. Java 用户如何使用
`Scrollbar`、`ScrollPaneAdjustable` 等可调组件实现此接口，描述数值可调范围/步进/当前值：
```java
Adjustable a = new Scrollbar(Adjustable.VERTICAL, 0, 1, 0, 100);
a.setValue(50); int v = a.getValue();
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Adjustable.java`（接口 + 常量）。
- `src/java.desktop/share/classes/java/awt/Scrollbar.java`、`java/awt/ScrollPaneAdjustable.java` 实现该接口。
- `test/jdk/java/awt/Scrollbar/`、`test/jdk/java/awt/ScrollPane/` 通过 Adjustable 引用设置/读取滚动条状态。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 常量精确值；接口白盒（成员方法存在）。
- 极简内存实现：`orientation` 回读、`min/max/value` 状态机、`unit/block/visible` 回读、`unit<=block` 组合链。
headless-safe（不依赖真实 Scrollbar）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：三方向常量；最小实现全套往返；方向常量两两不同。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
