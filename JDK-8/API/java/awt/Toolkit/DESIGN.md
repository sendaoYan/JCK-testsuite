# java.awt.Toolkit 黑盒行为测试设计（JDK-8）

## 1. API 功能
抽象类，AWT 与平台桥接：getDefaultToolkit 单例、getProperty；getScreenSize/getScreenResolution/
getScreenInsets/getColorModel；getFontList/getFontMetrics；图像 getImage(String/URL 缓存)与
createImage(String/URL/byte[]/byte[],off,len/ImageProducer 恒新)、prepareImage/checkImage；
getSystemEventQueue/getSystemClipboard/getSystemSelection/sync/beep；setDynamicLayout/isDynamicLayoutActive、
getLockingKeyState/setLockingKeyState、getBestCursorSize/getMaximumCursorColors/createCustomCursor、
getMenuShortcutKeyMask、areExtraMouseButtonsEnabled、isFrameStateSupported、getDesktopProperty、
AWTEventListener 注册族、mapInputMethodHighlight。

## 2. Java 用户如何使用
`Toolkit tk = Toolkit.getDefaultToolkit(); Image a = tk.getImage("icon.png");`
同名第二次 getImage 拿同一实例（缓存）；createImage 每次新实例；prepareImage+MediaTracker 确定性加载。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Toolkit.java`
（getImage 按名/URL 走缓存表；createImage 恒新；getLockingKeyState 非法 key IAE、不支持 UOE；
getProperty 未命中返回默认值）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集，含 getPrintJob/createDragSourceContextPeer 等）→ `testStructure`。
- 环境读取（屏幕尺寸/分辨率/逻辑字体 Dialog/色彩模型/beep/sync/getProperty fallback/getScreenInsets 非负）、
  MemoryImageSource 绘制红像素 → `testBlackBox`。
- **图像缓存契约（易错）**：getImage(String) 同名同实例、getImage(URL) 相等 URL 同实例、
  createImage(String) 新且异于缓存、createImage(byte[])（内嵌 1x1 GIF）与 off,len 重载恒新
  → `testImageCache`。
- 加载管线：prepareImage boolean、MediaTracker waitForID(0,10000) 完成、
  prepareImage true、checkImage ALLBITS 无 ERROR → `testImageLoading`。
- 平台契约：锁定键 VK_CAPS/VK_NUM bool-or-UOE、VK_A 恒 IAE；光标 0x0 全无或全有；
  **getMenuShortcutKeyMask()!=0（JDK-8 无 getMenuShortcutKeyMaskEx——JDK-9+ 新增，本侧不断言）**；
  areExtraMouseButtonsEnabled 默认 true；isFrameStateSupported(NORMAL) true/(99) false；
  getDesktopProperty(未知) null；事件队列稳定同一实例；AWTEventListener 闭环（JDK-8 匿名类写法）。
- 反例与精确异常：`getLockingKeyState(VK_A)`→IllegalArgumentException；
  合法键不支持时 get/set→UnsupportedOperationException（条件断言）。
- JDK-8 侧 104 断言通过（DISPLAY=:77，@key headful）。
