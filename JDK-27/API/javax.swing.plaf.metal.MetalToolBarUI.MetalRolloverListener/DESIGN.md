# javax.swing.plaf.metal.MetalToolBarUI.MetalRolloverListener — DESIGN

## 1. API 功能
`MetalToolBarUI.MetalRolloverListener` 是 `MetalToolBarUI` 的 protected 内部类（`extends BasicToolBarUI.PropertyListener`），监听工具栏属性变化。因非 public，用户无法从包外直接实例化，本测试退化为纯白盒验证。注：Metal 的 `createRolloverListener()` 默认返回 `null`，即默认 Metal 下不会挂载。

## 2. Java 用户如何使用
用户不直接使用该内部类；`MetalToolBarUI` 通过 `createRolloverListener()` 决定是否挂载，但 Metal 默认返回 `null`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:379`：`protected class MetalRolloverListener`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:274`：`protected PropertyChangeListener createRolloverListener()`（返回 `null`）。

## 4. 测试用例设计
本类为非 public protected 内部类，黑盒（包外实例化）不可行，退化为白盒验证：类名、修饰符（非 public、protected）、继承关系、构造器数量，仅验证反射可达性与结构。
