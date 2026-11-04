# javax.imageio.IIOException — DESIGN（JDK-8）

## 1. API 功能
`IIOException` 是 `java.io.IOException` 的直接子类，用于图像读写过程中的运行错误。两个公共构造器：
- `IIOException(String message)`：消息构造，`cause` 为 null（后续可 `initCause` 一次）。
- `IIOException(String message, Throwable cause)`：消息 + 原因链（此后 `initCause` 抛 IllegalStateException）。
无自有方法，行为全部继承 `Throwable`/`IOException`。

## 2. Java 用户如何使用
通常不直接构造，而是捕获：`try { ImageIO.read(file); } catch (IIOException e) { ... }`（`ImageIO.read(不存在文件)` 就抛它）。自定义 reader/writer 时 `throw new IIOException("解码失败", cause)` 包装底层异常。

## 3. 开源代码实际用法调研（来源：~/git/jdk 系源码与 test/jdk，仅借鉴思路）
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`read(File)` 打不开文件时 `throw new IIOException("Can't read input file!")`。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`getDestination(...)` 等抛 `new IIOException(...)`。
- `test/jdk/javax/imageio/ImageIOWriteNull.java`：异常类型区分是常见断言点（IAE vs IIOException）。
- JDK-8 的 `IIOException` 无显式 `serialVersionUID` 字段（JDK-9+ 增加了显式字段，实测双侧 `ObjectStreamClass.lookup` 的 SUID 均为 -3216210718638985251L，序列化互通）。

## 4. 测试用例设计 + 易错规范细节（`IIOExceptionTest`）
- 构造矩阵：单参消息往返/无 cause；单参 null 消息保留；双参消息+cause 身份；双参 null 消息仍记 cause；双参 null cause 容忍。
- 序列化（实测 JDK-8）：SUID 计算值 -3216210718638985251L 且**无显式声明字段**（与 JDK-9+ 的显式字段形成版本差异，双侧值一致）；round-trip 后 message 保留、cause 保留但为**深拷贝**（非同引用）；单参序列化后 cause 仍 null。
- Throwable 契约：`initCause` 单参构造后成功且返回 this、二次抛 IllegalStateException、双参构造后再 initCause 抛 IllegalStateException；`addSuppressed/getSuppressed` 顺序保留；`toString` 精确格式 `javax.imageio.IIOException: msg`；`getStackTrace` 首帧为本测试类；`getLocalizedMessage` 默认同 getMessage；多态 catch 为 IOException；cause 链终止探测。
- 白盒：extends IOException、两个公共构造器、无自有 declared methods、Serializable（经 IOException）。
- 易错点：不要断言 `getCause()` 在单参构造后非 null（为 null）；序列化后的 cause 是拷贝，断言身份会失败。
