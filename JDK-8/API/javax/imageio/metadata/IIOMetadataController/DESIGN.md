# javax.imageio.metadata.IIOMetadataController — DESIGN（JDK-8）

## 1. API 功能
`IIOMetadataController` 是单方法接口，为 `IIOMetadata` 提供交互式参数设置入口（通常为 GUI，也可无界面）：
- `boolean activate(IIOMetadata metadata)`：处理给定元数据；返回 true 表示设置完成可继续写出，false 表示取消、元数据不应被改动。
接口无字段、无注解（未标 `@FunctionalInterface` 但为 SAM 形状，可作 lambda/方法引用目标）、无 default 方法、无父接口；`activate` 声明无受检异常。

## 2. Java 用户如何使用
实现控制器（或 lambda）交给 `IIOMetadata.setController(...)`，由 `IIOMetadata.activateController()` 回调：
`IIOMetadataController c = md -> { /* 编辑 */ return true; }; md.setController(c); if (md.activateController()) { /* 写图 */ }`
`activate` 收到当前 `IIOMetadata` 实例；`activateController` 的返回值就是控制器返回值。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + jdk/test，只借鉴思路）
- `jdk/src/share/classes/javax/imageio/metadata/IIOMetadataController.java`：接口仅声明 `boolean activate(IIOMetadata metadata)`。
- `jdk/src/share/classes/javax/imageio/metadata/IIOMetadata.java`：`activateController()` 先 `hasController()`（否则 `IllegalStateException`），再 `return getController().activate(this)`——参数就是元数据自身。
- JDK 内部 GUI 控制器（如 gif/jpeg 插件关联的编辑对话框）实现该接口，返回值决定写操作是否继续。
- `jdk/test/javax/imageio/` 各处用 lambda 式桩控制器驱动 `activateController` 路径。

## 4. 测试用例设计 + 易错规范细节（`IIOMetadataControllerTest`）
- 白盒（testStructure）：接口、public、无父接口、无字段、恰好 1 个声明方法且 public abstract、返回 boolean、单参类型 `IIOMetadata`、无受检异常、`getMethods()` 恰 1 个、无注解、未标 `@FunctionalInterface`（实测 `isAnnotationPresent` false）。
- 行为（testBehavior）：记录型控制器（false/true 两配置）验证返回值/调用计数/参数身份；重复激活；`activate(null)` 直达实现（接口层不做 null 检查）；两个实例状态独立；lambda 与匿名类两种实现形态；抛异常控制器把**同一异常对象**原样中继。
- 与 IIOMetadata 接线：`setController/getController` 身份、`hasController` 跟随、`activateController()` 透传返回值且回调参数 `==` 元数据自身（实测 `activate(this)`）、控制器内抛的 RuntimeException 原样穿出 `activateController`、`setController(null)` 清除后 `activateController` 抛 `IllegalStateException`、一个控制器可被多个元数据共享（计数累加）。
- 版本差异：无（双侧 javap 一致）。
