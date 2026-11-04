# java.awt.GraphicsDevice 黑盒行为测试设计

## 1. API 功能
抽象类，代表一个图形设备（屏幕/打印机/图像缓冲）。
- 常量：`TYPE_RASTER_SCREEN(0)`、`TYPE_PRINTER(1)`、`TYPE_IMAGE_BUFFER(2)`。
- 查询：`getType()`、`getIDstring()`、`getConfigurations()`、`getDefaultConfiguration()`、`getBestConfiguration(GraphicsConfigTemplate)`。
- 显示模式：`getDisplayMode()`、`getDisplayModes()`、`setDisplayMode(DisplayMode)`、`isDisplayChangeSupported()`。
- 全屏：`isFullScreenSupported()`、`setFullScreenWindow(Window)`、`getFullScreenWindow()`。
- 其他：`getAvailableAcceleratedMemory()`、`isWindowTranslucencySupported(WindowTranslucency)`。

## 2. Java 用户如何使用
```java
GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
int type = dev.getType();            // TYPE_RASTER_SCREEN
String id = dev.getIDstring();       // ":0.0"
DisplayMode dm = dev.getDisplayMode();
DisplayMode[] modes = dev.getDisplayModes();
GraphicsConfiguration gc = dev.getDefaultConfiguration();
```
组合链路：`getDefaultScreenDevice` → `getType/getIDstring/getDisplayMode(s)/getConfigurations/getDefaultConfiguration` 结构枚举，不触碰全屏/改分辨率。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/FullScreen/CurrentDisplayModeTest/CurrentDisplayModeTest.java`：`getDefaultScreenDevice()` + `getDisplayMode/getDisplayModes` 枚举。
- `test/jdk/java/awt/FullScreen/DisplayModeNoRefreshTest.java`：`getDisplayModes` 遍历验证 refresh rate。
- JDK 源码 `src/java.desktop/share/classes/java/awt/GraphicsDevice.java`：`getDisplayMode()` 默认由 `getDefaultConfiguration().getBounds()`+`getColorModel().getPixelSize()` 合成；`getDisplayModes()` 默认返回 `{getDisplayMode()}`；`getBestConfiguration` 委派 `gct.getBestConfiguration(getConfigurations())`；`getFullScreenWindow()` 初始 null。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，@key headful，Xvfb :98，铁律：不断言具体分辨率/刷新率/加速内存数值）：
- 三个设备类型常量精确值（0/1/2）。
- `getScreenDevices()` 各设备 `getType()==TYPE_RASTER_SCREEN`、`getIDstring()` 非空。
- `getDisplayMode()` 非 null 且宽高 >0；`getDisplayModes()` 非空、各模式宽高 >0。
- `getConfigurations()` 非空、全部非 null；`getDefaultConfiguration()` 非 null。
- `getFullScreenWindow()` 初始为 null（未进入全屏）。
- `getBestConfiguration(匿名模板)` 委派返回 `configs[0]`。
断言 44 项，白盒 18 + 黑盒 26。
