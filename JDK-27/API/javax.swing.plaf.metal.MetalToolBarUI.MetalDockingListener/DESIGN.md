# javax.swing.plaf.metal.MetalToolBarUI.MetalDockingListener — DESIGN

## 1. API 功能
`MetalToolBarUI.MetalDockingListener` 是 `MetalToolBarUI` 的 protected 内部类（`extends DockingListener`），处理工具栏拖拽停靠交互。因非 public，用户无法从包外直接实例化，本测试退化为纯白盒验证。

## 2. Java 用户如何使用
用户不直接使用该内部类；`MetalToolBarUI` 通过 `createDockingListener()` 返回 `new MetalDockingListener(toolBar)` 自动挂载。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:390`：`protected class MetalDockingListener extends DockingListener`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:278`：`protected MouseInputListener createDockingListener()`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:280`：`return new MetalDockingListener(toolBar)`。

## 4. 测试用例设计
本类为非 public protected 内部类，黑盒（包外实例化）不可行，退化为白盒验证：类名、修饰符（非 public、protected）、继承关系、构造器数量，仅验证反射可达性与结构。
