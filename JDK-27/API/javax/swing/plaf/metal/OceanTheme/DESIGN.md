# javax.swing.plaf.metal.OceanTheme — DESIGN

## 1. API 功能
`OceanTheme` 是 JDK 默认的 Metal 具体主题（`extends DefaultMetalTheme`，非抽象）。方法分组：`getName()`（返回 `"Ocean"`）、`addCustomEntriesToTable(UIDefaults)` 注入海洋主题专属 UI 资源、`getPrimary1` 等颜色方法，以及继承的字体/颜色契约。

## 2. Java 用户如何使用
用户通过 `MetalLookAndFeel.setCurrentTheme(new OceanTheme())` 启用海洋主题；直接使用时：`OceanTheme t = new OceanTheme(); String n = t.getName(); UIDefaults d = new UIDefaults(); t.addCustomEntriesToTable(d); ColorUIResource c = t.getPrimary1();`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/OceanTheme.java:54`：`public class OceanTheme extends DefaultMetalTheme`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/OceanTheme.java:132`：`public void addCustomEntriesToTable(UIDefaults table)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/OceanTheme/4969419/bug4969419.java:182-183`：`MetalTheme theme = ...; if (theme == null || theme.getName() != "Ocean")`。

## 4. 测试用例设计（黑盒部分，`OceanThemeTest.testBlackBox`）
1. `getName()` 返回 `"Ocean"`。
2. `getPrimary1/getPrimary2/getSecondary1` 等颜色方法返回非空 `ColorUIResource`。
3. `addCustomEntriesToTable` 注入 `UIDefaults` 无异常。
