# java.awt.image.ImageObserver 设计案

## 1. API 功能分组
- 常量：`WIDTH/HEIGHT/PROPERTIES/SOMEBITS/FRAMEBITS/ALLBITS/ERROR/ABORT`。
- 方法：`imageUpdate(Image, int infoflags, int x, int y, int width, int height)`。

## 2. Java 用户用法
- 异步图像加载回调；`imageUpdate` 收到 infoflags 位掩码（如 ALLBITS）返回是否继续监听。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `java.awt.Component` 实现 ImageObserver 以重绘图像加载进度。
- `PixelGrabber`/图像加载相关测试用 `ALLBITS` 判断完成。
- JDK 内部 `Toolkit.prepareImage` 使用 ImageObserver 回调。

## 4. 测试设计（指向 ImageObserverTest.java）
- 白盒：interface、imageUpdate 签名存在。
- 黑盒：8 个常量精确值。
