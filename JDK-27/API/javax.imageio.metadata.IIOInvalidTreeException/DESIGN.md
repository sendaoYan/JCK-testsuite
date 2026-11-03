# javax.imageio.metadata.IIOInvalidTreeException — DESIGN

## 1. API 功能
`IIOInvalidTreeException` 继承 `javax.imageio.IIOException`，在 `IIOMetadata.setFromTree` / `mergeTree` 解析 DOM 元数据树失败时抛出。方法/字段分组：
- 构造器：`IIOInvalidTreeException(String message, Node offendingNode)`、`IIOInvalidTreeException(String message, Throwable cause, Node offendingNode)`。
- 查询：`getOffendingNode()` 返回导致解析失败的可疑 `org.w3c.dom.Node`（可能为 `null`，仅作为定位线索）。
- 继承：`getMessage()`、`getCause()`、`toString()`（来自 `Throwable`/`IIOException`）。
- 受保护字段：`protected Node offendingNode`。

## 2. Java 用户如何使用
用户不直接构造该异常，通常在捕获 `setFromTree` 抛出的 `IIOInvalidTreeException` 后用 `getOffendingNode()` 定位出错的节点：
`try { md.setFromTree(fmt, root); } catch (IIOInvalidTreeException e) { Node bad = e.getOffendingNode(); }`
测试中直接构造验证契约：`new IIOInvalidTreeException("bad", node).getOffendingNode() == node`；带原因版本 `new IIOInvalidTreeException("bad", cause, node)` 后 `getCause() == cause`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOInvalidTreeException.java:49`：类定义 `extends IIOException`，字段 `protected Node offendingNode`，两个构造器分别 `super(message)` 与 `super(message, cause)`。
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadata.java:492`：`mergeTree` 的 javadoc 声明解析失败时抛出 `IIOInvalidTreeException`。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGMetadata.java`（JDK 内 PNG 插件）：`mergeNativeTree` 等内部实现对非法节点 `throw new IIOInvalidTreeException(..., node)`。
- 使用方（ImageIO 用户）一般在 catch 后调用 `getOffendingNode()` 打印出错节点名。

## 4. 测试用例设计 + 自动化实现（`IIOInvalidTreeExceptionTest.testBlackBox`）
- 双参构造：message/getOffendingNode/cause(==null)/`instanceof IIOException`/`toString` 包含 message。
- 三参构造：message/getCause/getOffendingNode 三者精确往返。
- 边界：offendingNode 为 null、cause 为 null 时均按契约返回 null 而不抛异常。
- 白盒：非接口/非注解/非枚举、public、非 abstract、父类为 `javax.imageio.IIOException`、`offendingNode` 字段为 protected 且类型为 `org.w3c.dom.Node`。
