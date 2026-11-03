# javax.imageio.metadata.IIOMetadataController — DESIGN

## 1. API 功能
`IIOMetadataController` 是单方法接口，为 `IIOMetadata` 提供交互式参数设置入口（通常为 GUI，但也可为无界面实现）。方法：
- `boolean activate(IIOMetadata metadata)`：激活控制器处理给定元数据；返回 `true` 表示设置完成、可继续写出，返回 `false` 表示用户取消、元数据不应被改动。
接口只有一个抽象方法，因此可作为 lambda/函数式接口使用。

## 2. Java 用户如何使用
实现一个控制器（或直接用 lambda），交给 `IIOMetadata.setController(...)` 后由 `IIOMetadata.activateController()` 回调：
`IIOMetadataController c = md -> { /* 弹窗编辑 */ return true; }; md.setController(c); if (md.activateController()) { /* 写图 */ }`
`activate` 的实现会收到当前 `IIOMetadata` 实例作为参数，返回值决定是否继续。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadataController.java:29`：接口只声明 `boolean activate(IIOMetadata metadata)`。
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadata.java:880`：`IIOMetadata.activateController()` 默认实现先 `hasController()`，再 `return getController().activate(this)`；无控制器时抛 `IllegalStateException`。
- JDK 内部 GUI 插件（如 `com.sun.imageio.plugins.gif.GIFImageWriteParam` 关联的元数据控制器）通过实现该接口弹出编辑对话框，返回值决定写操作是否继续。

## 4. 测试用例设计 + 自动化实现（`IIOMetadataControllerTest.testBlackBox`）
- 用 PNG writer 的 `getDefaultImageMetadata` 取真实 `IIOMetadata` 作为 `activate` 入参。
- 记录型控制器 `RecordingController` 分别配置返回 false/true，断言 `activate` 返回值、调用次数、收到的参数身份。
- lambda 形式：`m -> true` / `m -> false` 验证函数式接口用法。
- 白盒：public abstract interface，仅一个抽象方法 `activate(IIOMetadata): boolean`。
