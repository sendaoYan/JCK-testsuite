# javax.imageio.event.IIOReadUpdateListener — DESIGN

## 1. API 功能
`IIOReadUpdateListener` 是 `java.util.EventListener` 的子接口（6 个方法，全 `public abstract void`），在像素实际可得时被调用：
`passStarted(ImageReader,BufferedImage,int pass,int minPass,int numPasses,int minX,int minY,int periodX,int periodY,int[] bands)`、`imageUpdate(...,int minX,int minY,int width,int height,int periodX,int periodY,int[] bands)`、`passComplete(ImageReader,BufferedImage)` 及三个 thumbnail 对应方法。
参数语义：`pass` 是当前扫描 pass 编号；PNG（Adam7）非隔行解码也报一个 pass（pass==0，且 numPasses 报 6 而非 1，见实测）；JPEG 渐进扫描的 `numPasses` 在解码中未知，报 `Integer.MAX_VALUE`。

## 2. Java 用户如何使用
`reader.addIIOReadUpdateListener(...)` 注册后在 `imageUpdate` 里增量重绘对应矩形区域：`minX,minY,width,height` 是脏矩形，`periodX/periodY` 是行/列采样步长，`bands` 为 null 表示全部波段。渐进解码用 `passStarted/passComplete` 切换"逐步清晰"的显示层。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/png/PNGImageReader.java`：`decodeImage` 对 `IHDR_interlaceMethod==0` 调 `decodePass(0,...)` 一次；隔行时按 Adam7 7 个 pass 循环 `decodePass(i, XOffset, YOffset, XSub, YSub, ...)`；`decodePass` 内先 `processPassStarted(theImage, passNum, sourceMinProgressivePass, sourceMaxProgressivePass, updateMinX, updateMinY, updateXStep, updateYStep, destinationBands)`，每完成一行 `processImageUpdate(...,1,...)`，pass 结束 `processPassComplete`。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/png/PNGImageWriter.java`：`write` 中 `switch(param.getProgressiveMode())`，`MODE_DEFAULT` → `IHDR_interlaceMethod=1`（写隔行 PNG 的正确姿势；`MODE_EXPLICIT` 注释标注 not allowed）。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageReader.java`：libjpeg 多扫描时 pass 计数，`numPasses` 用 `Integer.MAX_VALUE`（实测 10 扫描时每个 passStarted 均报 MAX_VALUE）。

## 4. 测试用例设计 + 易错规范细节
- 反射断言 6 个方法的精确签名集（`int[]` 参数在内）。
- 非隔行 PNG：`passStarted==1` 且 `(pass,minPass,numPasses,minX,minY,periodX,periodY)==(0,0,6,0,0,1,1)`、`bands==null`、传入的 `BufferedImage` 即目标图（8x8）；`imageUpdate` 每行一次（8 次），每个矩形满足 `x>=0, w>0, x+w<=width`、period>=1；`passComplete==1`。
- 隔行 PNG（PNGImageWriteParam + `setProgressiveMode(MODE_DEFAULT)` 写出）：7 个 pass 严格 0..6，每 pass 的 `minX/minY/periodX/periodY` 与 Adam7 表一致 `(0,0,8,8),(4,0,8,8),(0,4,4,8),(2,0,4,4),(0,2,2,4),(1,0,2,2),(0,1,1,2)`；解码像素级无损往返。
- 渐进 JPEG（`JPEGImageWriteParam.setProgressiveMode(MODE_DEFAULT)` 写出）：passStarted>=5（目标 JDK 实测 10）、pass 严格递增且 `minPass==0`、`numPasses==Integer.MAX_VALUE`、每 pass 均有 passComplete；baseline JPEG 恰好 1 个 pass（pass==0）。
- add/remove 契约与 progress 监听器相同（重复注册双回调、null no-op、removeAll 零回调）。
- 异常中继：`passStarted` 抛 RuntimeException → 读路径包成 `IIOException`，`getCause()` 为原异常（实测）。
- 易错点：非隔行 PNG 的 `numPasses` 上报 6（不是 1）——源码直接透传 `sourceMaxProgressivePass` 默认值；断言必须按实测而非直觉。
