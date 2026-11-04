# java.awt.Toolkit 黑盒行为测试设计

## 1. API 功能
抽象类，AWT 与本地平台（图形/剪贴板/事件队列/光标）的桥接。实例经 `Toolkit.getDefaultToolkit()` 获取。
- 静态：`getDefaultToolkit()`（单例）、`getProperty(String,String)`。
- 屏幕/渲染：`getScreenSize()`、`getScreenResolution()`、`getScreenInsets(gc)`、`getColorModel()`。
- 字体：`getFontList()`、`getFontMetrics(Font)`。
- 图像：`getImage(String/URL)`、`createImage(String/URL/byte[]/ImageProducer)`、`prepareImage`、`checkImage`。
- 事件/系统：`getSystemEventQueue()`、`getSystemClipboard()`、`getSystemSelection()`、`sync()`、`beep()`。
- 光标/打印：`createCustomCursor`、`getBestCursorSize`、`getMaximumCursorColors`、`getPrintJob`。

## 2. Java 用户如何使用
```java
Toolkit tk = Toolkit.getDefaultToolkit();
Dimension d = tk.getScreenSize();
int dpi = tk.getScreenResolution();
String[] logical = tk.getFontList();
Image img = tk.createImage(new MemoryImageSource(w, h, pixels, 0, w));
tk.beep();
```
组合链路：`getDefaultToolkit` → `getScreenSize/getScreenResolution/getFontList/getColorModel` 读取环境 → `createImage(ImageProducer)` 内存图像 → 绘制到 BufferedImage 断言像素。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/image/MultiResolutionImage/MultiResolutionImageObserverTest.java`：`Toolkit.getDefaultToolkit()` 创建图像 + `prepareImage`。
- `test/jdk/java/awt/TrayIcon/DisposeInActionEventTest.java`、`test/jdk/java/awt/Window/AlwaysOnTop/TestAlwaysOnTopBeforeShow.java`：`Toolkit.getDefaultToolkit()` 读取平台能力。
- JDK 源码 `src/java.desktop/share/classes/java/awt/Toolkit.java`：`getDefaultToolkit()` 静态 synchronized 单例；`getProperty` 先查 `platformResources`/`resources` 资源束，未命中返回 defaultValue；`createImage(ImageProducer)` 抽象，由 `sun.awt.X11.XToolkit` 实现为 `ToolkitImage`。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，@key headful，Xvfb :98 下运行）：
- `getDefaultToolkit()` 非 null 且单例（两次 ==）。
- `getScreenSize()` 非 null 且宽高 >0（不断言具体分辨率）；`getScreenResolution()` >0。
- `getFontList()` 非空且含逻辑字体 "Dialog"；`getColorModel()` 非 null 且 pixelSize>0。
- `beep()` 可调用不抛异常。
- `createImage(new MemoryImageSource(...))` 绘制到 BufferedImage 后像素为红（内存图像路径）。
- `getSystemEventQueue()` 非 null；`getProperty(未知key, fallback)` 返回 fallback；`getScreenInsets(gc)` 非 null。
断言 37 项，白盒 20 + 黑盒 17。
