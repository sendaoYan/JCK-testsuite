# javax.swing.plaf.metal.MetalSplitPaneDivider.MetalDividerLayout — DESIGN

## 1. API 功能
`MetalSplitPaneDivider.MetalDividerLayout` 是 `MetalSplitPaneDivider` 的 public 内部类（`implements LayoutManager`）。方法分组：`addLayoutComponent/removeLayoutComponent`、`layoutContainer(Container)` 布局左右按钮与分隔条、`minimumLayoutSize/preferredLayoutSize` 计算尺寸。

## 2. Java 用户如何使用
用户通过 `MetalSplitPaneDivider` 的布局管理器使用它。直接使用时：`MetalSplitPaneDivider d = new MetalSplitPaneDivider(sp); MetalDividerLayout l = d.new MetalDividerLayout(); l.layoutContainer(d); l.preferredLayoutSize(d);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSplitPaneDivider.java:318`：`public class MetalDividerLayout implements LayoutManager`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSplitPaneDivider.java:325`：`public void layoutContainer(Container c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSplitPaneDivider.java:377` 与 `:381`：`minimumLayoutSize/preferredLayoutSize`。

## 4. 测试用例设计（黑盒部分，`MetalDividerLayoutTest.testBlackBox`）
1. 通过反射取得 `MetalSplitPaneDivider` 构造器并实例化（先 `installUI` 到 `JSplitPane` 以建立分隔条）。
2. `d.new MetalDividerLayout()` 非空；`addLayoutComponent/removeLayoutComponent` 无异常。
3. `layoutContainer(d)` 无异常；`minimumLayoutSize/preferredLayoutSize` 返回非空。
