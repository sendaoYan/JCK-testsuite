# java.awt.MediaTracker 黑盒行为测试设计

## 1. API 功能
- 状态常量：`LOADING=1`、`ABORTED=2`、`ERRORED=4`、`COMPLETE=8`。
- 构造：`MediaTracker(Component)`。
- 管理：`addImage(Image,int)` / 带缩放尺寸重载、`removeImage` 三重重载。
- 查询：`checkID/checkAll`、`statusID/statusAll(load)`、`waitForID/waitForAll`（阻塞式与带超时）、`isErrorID/isErrorAny`、`getErrorsID/getErrorsAny`。

## 2. Java 用户如何使用
跟踪一组图片的异步加载状态（状态机）：
```java
MediaTracker mt = new MediaTracker(component);
mt.addImage(img, 1);
if (mt.checkID(1)) { /* id=1 的图全部完成 */ }
int s = mt.statusID(1, true); // LOADING/ABORTED/ERRORED/COMPLETE
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/MediaTracker.java`：`checkID(id)` 对「无该 id 图片」为真空真；`statusAll` 取各 entry 状态按位或。
- `test/jdk/java/awt/image/`、`test/jdk/java/awt/MediaTracker/` 用 MediaTracker 同步等待图片加载后绘图。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 状态常量精确值。
- 用已加载的 `BufferedImage`（无需真实异步加载）走状态机：`addImage → checkID/checkAll → statusID/statusAll==COMPLETE → waitForID/waitForAll 立即返回 → isError/getErrors 全空 → removeImage`。
- 三种 `removeImage` 重载 + 缩放 `addImage` 重载。
headless-safe（dummy `new Canvas()`，不触发真实图片加载）。
