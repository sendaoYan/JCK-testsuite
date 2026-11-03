# java.awt.GraphicsEnvironment 黑盒行为测试设计

## 1. API 功能
抽象类，描述本机图形环境（屏幕设备 + 可用字体）。
- 静态：`getLocalGraphicsEnvironment()`（单例）、`isHeadless()`。
- 设备：`getScreenDevices()`、`getDefaultScreenDevice()`。
- 图像上下文：`createGraphics(BufferedImage)`。
- 字体：`getAllFonts()`、`getAvailableFontFamilyNames()`、`getAvailableFontFamilyNames(Locale)`、`registerFont(Font)`。
- 几何/边界：`getCenterPoint()`、`getMaximumWindowBounds()`。
- 状态：`isHeadlessInstance()`、`preferLocaleFonts()`、`preferProportionalFonts()`。

## 2. Java 用户如何使用
```java
GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
if (!ge.isHeadless()) {
    GraphicsDevice dev = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = dev.getDefaultConfiguration();
    String[] fams = ge.getAvailableFontFamilyNames();
    Font[] all = ge.getAllFonts();
}
```
组合链路：`getLocalGraphicsEnvironment` → `getScreenDevices/getDefaultScreenDevice` → `getAvailableFontFamilyNames/getAllFonts` 枚举字体 → `createGraphics` 离屏渲染。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/FullScreen/CurrentDisplayModeTest/CurrentDisplayModeTest.java`、`FullScreen/8013581/bug8013581.java`：`getLocalGraphicsEnvironment()` + `getDefaultScreenDevice()` 枚举设备。
- `test/jdk/java/awt/FullScreen/FullScreenInsets/FullScreenInsets.java`：`getMaximumWindowBounds` 用法。
- JDK 源码 `src/java.desktop/share/classes/java/awt/GraphicsEnvironment.java`：`getLocalGraphicsEnvironment` 静态缓存单例；`getAllFonts/getAvailableFontFamilyNames` 抽象，由 `sun.java2d.SunGraphicsEnvironment` 实现（逻辑字体 Dialog/DialogInput/Monospaced/SansSerif/Serif 恒在）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，@key headful，Xvfb :98）：
- `getLocalGraphicsEnvironment` 非 null 且单例；`isHeadless()==false`、`isHeadlessInstance()` 与之一致。
- `getScreenDevices()` 非空、各设备非 null；`getDefaultScreenDevice()` 非 null 且属于 `getScreenDevices()`。
- `getAvailableFontFamilyNames()` 与 `(Locale.US)` 重载非空且含 "Dialog"。
- `getAllFonts()` 非空、全部非 null、含逻辑字体族 "Dialog"。
- `createGraphics(BufferedImage)` 返回 Graphics2D，填充红色后 getRGB 断言像素。
- `getCenterPoint()`、`getMaximumWindowBounds()` 非 null，最大窗口边界宽高 >0。
断言 43 项，白盒 17 + 黑盒 26。
