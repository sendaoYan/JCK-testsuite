# javax.swing.plaf.metal.MetalLookAndFeel — DESIGN

## 1. API 功能
`MetalLookAndFeel` 是 Metal（也称 Java 默认）观感（`extends BasicLookAndFeel`，非抽象）。它提供 `getDefaults()` 统一默认值表、`getDescription/getID/getName/isNativeLookAndFeel/isSupportedLookAndFeel`、以及静态 `setCurrentTheme(MetalTheme)/getCurrentTheme()` 主题切换。本测试不通过 `setLookAndFeel` 安装，只直接调用实例方法以避免全局状态污染。

## 2. Java 用户如何使用
`MetalLookAndFeel laf = new MetalLookAndFeel(); UIDefaults d = laf.getDefaults(); MetalLookAndFeel.setCurrentTheme(new OceanTheme());`。主题切换影响后续创建的组件外观。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:108`：`public class MetalLookAndFeel extends BasicLookAndFeel`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:1625`：`public static void setCurrentTheme(MetalTheme theme)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalLookAndFeel/Test8039750.java:38`：`UIDefaults table = new MetalLookAndFeel().getDefaults();`。

## 4. 测试用例设计（黑盒部分，`MetalLookAndFeelTest.testBlackBox`）
1. `getDescription/getID/getName` 非空；`isNativeLookAndFeel()` 为假；`isSupportedLookAndFeel()` 为真。
2. `getDefaults()` 返回非空且包含 `"ComboBoxUI"` 等关键键。
3. `setCurrentTheme`/`getCurrentTheme` 往返无异常（不安装观感）。
