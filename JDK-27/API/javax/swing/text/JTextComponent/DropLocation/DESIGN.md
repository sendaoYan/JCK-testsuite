# javax.swing.text.JTextComponent.DropLocation — DESIGN

## 1. API 功能
`JTextComponent` 的公开静态嵌套类（`public static final class DropLocation extends TransferHandler.DropLocation`），表示文本组件在一次拖放操作中的落点。
- 构造器为 `private DropLocation(Point p, int index, Position.Bias bias)`（DnD 系统是唯一工厂，用户无法直接 new）。
- 查询：`getIndex()`（插入的字符间隙索引）、`getBias()`（`Position.Bias.Forward/Backward`）、继承的 `getDropPoint()`、`toString()`。
- 配套组件 API：`JTextComponent.getDropLocation()`（DnD 前为 null）、`getDropMode()/setDropMode()`（默认 `DropMode.USE_SELECTION`，支持 `USE_SELECTION/INSERT`）。

## 2. Java 用户如何使用
拖放过程中从 `getDropLocation()` 读取当前落点 `getIndex()/getBias()`；通过 `setDropMode(DropMode.INSERT)` 切换插入模式；不支持的模式/`null` 抛 `IllegalArgumentException`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`DropLocation`）：`private DropLocation(Point p, int index, Position.Bias bias) { super(p); this.index = index; this.bias = bias; }`。
- 同文件 `getIndex()/getBias()`：分别返回 `index`/`bias`；`toString()` 拼出 `[dropPoint=...,index=...,bias=...]`。
- 同文件 `setDropMode/checkDropMode`：`USE_SELECTION/INSERT` 之外或 `null` 抛 `IllegalArgumentException`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/TransferHandler.java`（`DropLocation`）：`getDropPoint()` 返回 `new Point(dropPoint)` 副本；`dropPoint == null` 抛 `IllegalArgumentException`。

## 4. 测试用例设计（黑盒部分，`DropLocationTest.testBlackBox`）
1. `Position.Bias.Forward/Backward` 存在、互异、`toString()` 为 "Forward"/"Backward"。
2. 反射调用私有构造器实例化：`getIndex()/getBias()/getDropPoint()` 往返；`getDropPoint()` 返回副本而非同一对象；`toString()` 含类名/index/bias。
3. Forward bias 实例；`null` 落点 → IllegalArgumentException。
4. 组件侧：`getDropLocation()` 默认 null；`getDropMode()` 默认 USE_SELECTION；`setDropMode(INSERT)` 往返；`null`/`ON` → IllegalArgumentException。
