# javax.imageio.metadata.IIOInvalidTreeException — DESIGN（JDK-8）

## 1. API 功能
`IIOInvalidTreeException` 表示 `IIOMetadata.mergeTree/setFromTree` 无法解析输入 DOM 树。继承链：`IIOInvalidTreeException` → `IIOException` → `IOException`（可序列化）。恰好两个 public 构造器（javap 确认，**没有**单 String 版本）：
- `IIOInvalidTreeException(String message, Node offendingNode)`
- `IIOInvalidTreeException(String message, Throwable cause, Node offendingNode)`
查询：`getOffendingNode()` 返回可疑 `org.w3c.dom.Node`（可为 null，只是定位线索）。受保护字段 `protected Node offendingNode`。message/cause/toString/suppressed 走 `Throwable` 通用契约；双参构造器不固定 cause，可后补 `initCause`。

## 2. Java 用户如何使用
通常在 catch 块里定位坏节点：
`try { md.setFromTree(fmt, root); } catch (IIOInvalidTreeException e) { Node bad = e.getOffendingNode(); }`
测试中直接构造验证契约：`new IIOInvalidTreeException("bad", node).getOffendingNode() == node`；带 cause 版本 `getCause() == cause`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + jdk/test，只借鉴思路）
- `jdk/src/share/classes/javax/imageio/metadata/IIOInvalidTreeException.java`：两个构造器分别 `super(message)` 与 `super(message, cause)`，字段 `protected Node offendingNode`。
- `jdk/src/share/classes/com/sun/imageio/plugins/png/PNGMetadata.java`：`mergeNativeTree/mergeStandardTree` 对非法根/非法节点 `throw new IIOInvalidTreeException(..., node)`（错误根名消息形如 "Root must be <格式根名>"）。
- `jdk/test/javax/imageio/metadata/`（IIOMetadataFormat 子目录等）：多处 catch `IIOInvalidTreeException` 后检查 offending 节点定位。

## 4. 测试用例设计 + 易错规范细节（`IIOInvalidTreeExceptionTest`）
- 白盒：非接口/注解/枚举、public、具体类、父类 IIOException、祖父 IOException（`instanceof java.io.IOException`）；`offendingNode` protected 且类型 Node；反射断言恰好 2 个 public 构造器（(String,Node) 与 (String,Throwable,Node)——纠正任务简报里"有 (String) 单参构造器"的说法，javap 无此重载）；`getOffendingNode` 返回类型 Node。
- 双参构造：message 往返、`getLocalizedMessage` 等值、offendingNode 身份、cause 初始 null、`toString()` 前缀 `javax.imageio.metadata.IIOInvalidTreeException: <msg>`、`initCause` 后补成功但二次抛 `IllegalStateException`。
- 三参构造：message/cause/offendingNode 三重身份、构造器已设 cause 后 `initCause` 抛 `IllegalStateException`（实测消息 "Can't overwrite cause..."）。
- Throwable 机制：`addSuppressed/getSuppressed` 往返、`fillInStackTrace()` 返回 this、栈轨迹可读。
- null 语义：null offendingNode、null cause、null message 三者都合法并按 null 往返（不抛异常）。
- 活引用：offendingNode 是调用方节点的活引用（之后 setAttribute 在异常对象上仍可见）。
- 两实例同参数：引用不同但 message 相等、offendingNode 同一引用。
- 版本差异：无（双侧 javap 一致）。
