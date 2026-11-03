# javax.swing.plaf.metal.MetalTheme — DESIGN

## 1. API 功能
`MetalTheme` 是 Metal 观感的抽象主题基类（`public abstract class MetalTheme`）。方法分组：`getName()` 名称、字体组（`getControlTextFont/getSystemTextFont/getUserTextFont/getMenuTextFont/getWindowTitleFont/getSubTextFont`）、颜色组（`getPrimary1/2/3`、`getSecondary1/2/3`、`getBlack/getWhite` 等语义色 `getFocusColor/getControl/getDesktopColor`）。本类为抽象类，测试仅通过 `OceanTheme` 等具体实现间接覆盖其契约。

## 2. Java 用户如何使用
用户通过 `MetalLookAndFeel.setCurrentTheme(MetalTheme)` 设置主题，或用 `DefaultMetalTheme`/`OceanTheme` 具体子类直接获取字体与颜色：`MetalTheme t = new OceanTheme(); FontUIResource f = t.getControlTextFont(); ColorUIResource c = t.getPrimary1();`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTheme.java:66`：`public abstract class MetalTheme`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTheme.java:89`：`public abstract String getName()`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/OceanTheme/4969419/bug4969419.java:155`：`static MetalTheme oceanTheme;`。

## 4. 测试用例设计（黑盒部分，`MetalThemeTest.testBlackBox`）
本类为抽象类，黑盒通过 `OceanTheme`（JDK 默认 Metal 主题）间接验证：`getName()` 非空、字体组返回非空 `FontUIResource`、颜色组（`getPrimary1/getControl/getFocusColor` 等）返回非空 `ColorUIResource`。
