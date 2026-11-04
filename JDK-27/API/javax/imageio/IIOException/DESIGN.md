# javax.imageio.IIOException — DESIGN

## 1. API 功能
`IIOException` 是 `java.io.IOException` 的子类，用于表示图像 I/O（`ImageIO`、`ImageReader`/`ImageWriter`）在读取/写入过程中发生的错误。构造器两个：
- `IIOException(String message)`：只带消息，`cause` 为 null。
- `IIOException(String message, Throwable cause)`：带消息和原因链。

继承自 `IOException` 的所有行为（`getMessage`/`getCause`/序列化）均可用。

## 2. Java 用户如何使用
用户不直接创建 `IIOException`，而是在 `ImageIO.read(...)`/`ImageIO.write(...)` 抛出的 `IOException` 分支里捕获它：
`try { ImageIO.read(file); } catch (IIOException e) { e.getMessage(); e.getCause(); }`。
直接构造则用 `throw new IIOException("解码失败", cause)` 包装底层异常。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`read(ImageInputStream)` 捕获 `RuntimeException` 后 `throw new IIOException(e.toString(), e)`（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`getDestination(...)` 抛出 `new IIOException(...)`（源码内）。
- `test/jdk/javax/imageio/ImageIOWriteNull.java`：验证 `ImageIO.write(null,null,(File)null)` 抛 `IllegalArgumentException`，说明异常类型区分是常见断言点。

## 4. 测试用例设计 + 自动化实现（`IIOExceptionTest.testBlackBox`）
- 单参构造：消息往返、无 cause、`instanceof IOException`。
- null 消息单参构造：消息为 null。
- 双参构造：消息与 cause 身份、`instanceof Serializable`。
- 双参 + null 消息：cause 仍被记录。
- 白盒部分补齐：public 非抽象、继承 `IOException`、两个构造器、Serializable。
