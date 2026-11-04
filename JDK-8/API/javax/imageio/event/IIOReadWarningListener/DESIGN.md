# javax.imageio.event.IIOReadWarningListener — DESIGN

## 1. API 功能
单方法接口：`warningOccurred(ImageReader source, String warning)`，`void`、无 checked 异常。解码器遇到**可恢复**问题时调用（致命错误直接抛 `IOException`）。JPEG 解码警告同时会被 libjpeg 打到 stderr，属正常现象。

## 2. Java 用户如何使用
`reader.addIIOReadWarningListener(l)` 注册收集警告日志，`removeIIOReadWarningListener(l)`/`removeAllIIOReadWarningListeners()` 移除。`add(null)`/`remove(null)` 是文档化 no-op；重复注册同一实例 → 同一警告回调两次。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageReader.java`：`processWarningOccurred(String)` 遍历 `warningListeners` 并以平行 `warningLocales` 列表做消息本地化。
- libjpeg 行为（`~/git/jdk8u-dev/jdk/src/share/native/libjavajpeg/`）：熵数据结束后、EOI 前的杂散字节产生 "Corrupt JPEG data: N extraneous bytes before marker 0xd9" 警告且解码继续——这是本测试稳定触发 warningOccurred 的输入构造方式。
- `test/jdk/javax/imageio/` 无专门 warning 回归；思路为「构造可恢复损坏输入」。

## 4. 测试用例设计 + 易错规范细节
- 反射断言唯一方法精确签名 `(ImageReader,String)`、void、无 default。
- 端到端（实测两侧 JDK 行为一致）：有效 16x16 baseline JPEG 的最终 EOI 前插入 3 个杂散字节 `{0x42,0x42,0x42}` → 解码成功（16x16）、`warningOccurred` 恰好触发、消息含 `"extraneous bytes before marker"`、`source==reader`；移除监听器后重读 → 零回调。
- 干净输入（PNG、baseline JPEG）解码 → 零警告。
- 注册语义：重复 add → 同一警告回调 2 次；remove 一次 → 1 次；再 remove → 0；remove 未注册/no-op；null add/remove 不抛；removeAll → 0。
- 直调契约：`(reader,"msg")` 参数原样转交；`(null,null)`、负数、空串都被接口接受（不做校验）；实现类 `instanceof EventListener`。
- 多监听器：两个不同实例都收到同一警告，且按注册顺序分发。
