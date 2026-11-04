# javax.imageio.event.IIOReadUpdateListener — DESIGN

## 1. API 功能
`IIOReadUpdateListener` 是 `java.util.EventListener` 子接口，用于报告解码过程中像素块更新。六个方法：`passStarted`、`imageUpdate`、`passComplete`、`thumbnailPassStarted`、`thumbnailUpdate`、`thumbnailPassComplete`。

## 2. Java 用户如何使用
注册到 reader 以逐步显示渐进式图像：`reader.addIIOReadUpdateListener(listener);` 在 `imageUpdate` 里把 `BufferedImage` 区域刷新到界面。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`processPassStarted`/`processImageUpdate`/`processPassComplete` 向监听器分发更新（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`addIIOReadUpdateListener` 维护更新监听器列表。

## 4. 测试用例设计 + 自动化实现（`IIOReadUpdateListenerTest.testBlackBox`）
- 匿名实现六个方法直接调用均不抛异常。
- 挂到真实 PNG reader 后 read：断言 passStarted/passComplete 各触发一次。
- 白盒：isInterface、extends EventListener、六个公共方法存在。
