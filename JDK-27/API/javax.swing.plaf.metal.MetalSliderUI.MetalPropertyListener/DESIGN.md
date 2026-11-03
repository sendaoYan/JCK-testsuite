# javax.swing.plaf.metal.MetalSliderUI.MetalPropertyListener — DESIGN

## 1. API 功能
`MetalSliderUI.MetalPropertyListener` 是 `MetalSliderUI` 的 protected 内部类（`extends BasicSliderUI.PropertyChangeHandler`）。它监听滑杆属性变化并触发重绘。因非 public，用户无法从包外直接实例化，本测试退化为纯白盒验证。

## 2. Java 用户如何使用
用户不直接使用该内部类；`MetalSliderUI` 在 `installUI` 阶段通过 `createPropertyChangeListener` 返回 `new MetalPropertyListener()` 自动挂载。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSliderUI.java:168`：`protected class MetalPropertyListener extends BasicSliderUI.PropertyChangeHandler`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSliderUI.java:172`：`protected MetalPropertyListener() {}`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSliderUI.java:162`：`return new MetalPropertyListener()`。

## 4. 测试用例设计
本类为非 public protected 内部类，黑盒（包外实例化）不可行，退化为白盒验证：类名、修饰符（非 public、protected、非 abstract）、继承关系、构造器数量，仅验证反射可达性与结构。
