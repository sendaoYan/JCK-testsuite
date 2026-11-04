# java.awt.Toolkit 黑盒行为测试设计

## ① API 功能
抽象类，AWT 与本地平台（图形/剪贴板/事件队列/光标）的桥接。实例经 `Toolkit.getDefaultToolkit()` 获取。
- 静态：`getDefaultToolkit()`（单例）、`getProperty(String,String)`。
- 屏幕/渲染：`getScreenSize()`、`getScreenResolution()`、`getScreenInsets(gc)`、`getColorModel()`。
- 字体：`getFontList()`、`getFontMetrics(Font)`。
- 图像缓存与加载：`getImage(String/URL)`（**按名字/URL 缓存同实例**）、`createImage(String/URL/byte[]/byte[],off,len/ImageProducer)`（**每次新实例**）、`prepareImage/checkImage`、MediaTracker 确定性加载。
- 事件/系统：`getSystemEventQueue()`、`getSystemClipboard()`、`getSystemSelection()`、`sync()`、`beep()`、`addAWTEventListener/removeAWTEventListener/getAWTEventListeners`。
- 平台能力：`setDynamicLayout/isDynamicLayoutActive`、`getLockingKeyState/setLockingKeyState`、`getBestCursorSize/getMaximumCursorColors/createCustomCursor`、`getMenuShortcutKeyMask`（+JDK-9 起 `getMenuShortcutKeyMaskEx`）、`areExtraMouseButtonsEnabled`、`isFrameStateSupported`、`getDesktopProperty`、`mapInputMethodHighlight`。

## ② Java 用户如何使用
```java
Toolkit tk = Toolkit.getDefaultToolkit();
Dimension d = tk.getScreenSize();
Image img = tk.getImage("icon.png");   // 第二次同名调用拿同一实例（缓存）
Image fresh = tk.createImage(bytes);   // 每次新实例
tk.prepareImage(img, -1, -1, null);
```
组合链路：`getDefaultToolkit` → 环境读取 → getImage/createImage 缓存语义对比 → prepareImage/MediaTracker 加载闭环 → 平台能力探针。

## ③ 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/image/MultiResolutionImage/MultiResolutionImageObserverTest.java`：`Toolkit.getDefaultToolkit()` 创建图像 + `prepareImage`。
- `test/jdk/java/awt/TrayIcon/DisposeInActionEventTest.java`：`Toolkit.getDefaultToolkit()` 读取平台能力。
- JDK 源码 `src/java.desktop/share/classes/java/awt/Toolkit.java`：`getDefaultToolkit()` 静态 synchronized 单例；`getProperty` 未命中返回 defaultValue；`getImage(String/URL)` 经内部缓存表返回同实例，`createImage` 恒新实例；`getLockingKeyState` 非法 key 抛 IAE、平台不支持抛 UOE。

## ④ 测试用例设计 + 自动化实现
场景（@key headful，DISPLAY=:77）：
- `getDefaultToolkit()` 非 null 且单例；`getScreenSize/getScreenResolution/getColorModel/getFontList`（含逻辑字体 Dialog）环境读取；`beep/sync` 可调用；`getProperty(未知key, fallback)` 返回 fallback；`getScreenInsets(gc)` 非负 → `testBlackBox`。
- `createImage(MemoryImageSource)` 绘制到 BufferedImage 断言红像素 → `testBlackBox`。
- **图像缓存契约**：`getImage(String)` 同名同实例；`getImage(URL)` 相等 URL 同实例；`createImage(String)` 每次新且异于缓存项；`createImage(byte[])`（内嵌 1x1 GIF 字节）与 `(byte[],off,len)` 每次新实例 → `testImageCache`。
- **加载管线**：`prepareImage` 返回 boolean；MediaTracker `waitForID(0,10000)` 确定性完成；完成后 `prepareImage==true`、`checkImage` 含 ALLBITS 且无 ERROR → `testImageLoading`。
- **平台契约**：`setDynamicLayout(true/false)` 可调用；锁定键 VK_CAPS/VK_NUM boolean-or-UOE、**VK_A 恒 IAE**、setLockingKeyState 可调用-or-UOE；`getBestCursorSize(32,32)` 非负且 0x0 全无或全有（支持时 `createCustomCursor` 非 null、颜色数>=1）；`getMenuShortcutKeyMask()!=0` 且 **JDK-27 独有 `getMenuShortcutKeyMaskEx()!=0`**；`areExtraMouseButtonsEnabled()==true` 默认；`isFrameStateSupported(NORMAL)==true`、`(99)==false`；`getDesktopProperty(未知)==null`；剪贴板非 null、selection clipboard-or-null；`getSystemEventQueue()` 稳定同一实例；AWTEventListener 注册/过滤查询/移除闭环；`getFontMetrics` 非 null；`mapInputMethodHighlight` 可调用 → `testPlatformContracts`。
通过 82 断言。

## ⑤ 反例与精确异常
- `getLockingKeyState(非锁定键如 VK_A)` → IllegalArgumentException（所有平台）。
- `getLockingKeyState/setLockingKeyState(合法键)` 平台不支持 → UnsupportedOperationException（条件断言 bool-or-UOE）。
- 版本差异：**`getMenuShortcutKeyMaskEx` 为 JDK-9+ 新增**（JDK-8 侧无此 API，不断言；两版 `getMenuShortcutKeyMask()` 均为 2/CTRL）。
