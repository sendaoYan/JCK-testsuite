# javax.imageio.event.IIOWriteWarningListener — DESIGN

## 1. API 功能
单方法接口：`warningOccurred(ImageWriter source, int imageIndex, String warning)`，`void`、无 checked 异常。编码器遇到可恢复问题（如质量取舍、元数据降级）时分发；致命错误直接抛 `IOException`。

## 2. Java 用户如何使用
`writer.addIIOWriteWarningListener(l)` 注册收集编码警告，`removeIIOWriteWarningListener(l)`/`removeAllIIOWriteWarningListeners()` 移除。`add(null)`/`remove(null)` 文档化 no-op；重复注册同一实例 → 双回调。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageWriter.java`：`processWarningOccurred(int imageIndex, String warning)` 遍历 warningListeners + 平行 warningLocales 列表。
- 探针实测（JDK-8 与 JDK-27 一致）：PNG/JPEG/BMP 干净编码、调色板源 JPEG 编码、RLE BMP 编码均不触发 warningOccurred；**无稳定驱动写侧警告的真实输入**（记录为「无法稳定驱动」，接口契约以 add/remove + 零回调断言覆盖）。
- `test/jdk/javax/imageio/` 无写侧警告专门回归。

## 4. 测试用例设计 + 易错规范细节
- 反射断言唯一方法精确签名 `(ImageWriter,int,String)`、void、无 default、无字段。
- 干净写零回调：PNG/JPEG/BMP 各写一张固定渐变图，断言 0 警告且字节非空；调色板源（TYPE_BYTE_INDEXED）JPEG 写同样 0 警告。
- 注册语义：重复 add 不抛、逐次 remove、remove 未注册 no-op、null add/remove 不抛、removeAll 后写零回调。
- 直调契约：`(writer,0,"w-1")` 三参数原样转交；`(null,-1,null)` 接口层不做校验；`getMethod(ImageWriter,int,String)` 可解析；实现类是 `EventListener`。
- 多监听器注册顺序分发（经直调验证）。
