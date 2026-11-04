# javax.imageio.event.IIOReadProgressListener — DESIGN

## 1. API 功能
`IIOReadProgressListener` 是 `java.util.EventListener` 的子接口（9 个方法，全部 `public abstract void`、无 checked 异常、无 default 方法），由 `ImageReader` 在解码进度变化时分发：
`sequenceStarted(ImageReader,int minIndex)`、`imageStarted(ImageReader,int)`、`imageProgress(ImageReader,float percentageDone)`、`imageComplete(ImageReader)`、`thumbnailStarted/thumbnailProgress/thumbnailComplete`、`readAborted(ImageReader)`、`sequenceComplete(ImageReader)`。
百分比语义：`percentageDone` ∈ [0.0, 100.0]，单调不减（同一图内）。

## 2. Java 用户如何使用
实现接口并 `reader.addIIOReadProgressListener(this)` 注册；`read()`/`readAll()` 驱动回调；`removeIIOReadProgressListener(l)` 逐次移除，`removeAllIIOReadProgressListeners()` 全清。进度条场景在 `imageProgress` 里更新 UI。注册表是 `List`：**同一实例 add 两次 → 每个事件回调两次**；`add(null)`/`remove(null)` 是文档化的 no-op。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageReader.java`：`addToList` 不去重（`l.add(elt)` 直接追加）；`removeFromList` 按 `indexOf` 移除单个出现；`processSequenceStarted/processImageStarted/...` 逐个调用已注册监听器。
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageReader.java`（`readAll(Iterator)`）：先 `processSequenceStarted(minIndex)`，循环 `read(imageIndex, param)` 直到 `IndexOutOfBoundsException`，最后 `processSequenceComplete()`——这就是本测试用 `readAll` 驱动 sequence 事件的依据。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/png/PNGImageReader.java`：`read` 按行 `processImageProgress`（8 行 → 12.5/25/.../100 共 8 次）。
- `test/jdk/javax/imageio/` 下监听器无专门回归；思路为「真实编解码器驱动事件序列」。

## 4. 测试用例设计 + 易错规范细节
- 反射断言 9 个方法的**精确签名集**（与 javap 双侧 diff 完全一致）、无字段/无成员类型。
- 端到端（真实 PNG 编解码器）：`ImageIO.write` 小图 → `MemoryCacheImageInputStream` → `ImageIO.getImageReader(writer)` → 注册记录型 listener → `readAll(emptyIterator)`：断言 `sequenceStarted==1（minIndex 0，source==reader）`、`imageStarted==1（imageIndex 0）`、`imageProgress>=1` 且单调且最后==100.0、`imageComplete==1`、`sequenceComplete==1`、无 thumbnail/abort 事件。
- `read(0,null)` 不触发 sequence 事件（单图协议）；事件计数与 readAll 对照。
- add/remove 契约：重复 add → 双回调；移除一次 → 单回调；remove 未注册/no-op；null add/remove 不抛；`removeAll` 后再读零回调。
- 异常中继（易错点）：回调抛出的 `RuntimeException` 会被 PNGImageReader 包成 `javax.imageio.IIOException`，`getCause()==原异常`——**读路径不透传原样异常**（与写路径不同），实测记录。
- 移除监听器后重读同一流必须零回调（防「removeAll 不生效」缺陷）。
