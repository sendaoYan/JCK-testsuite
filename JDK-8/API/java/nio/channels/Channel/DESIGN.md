# java.nio.channels.Channel 设计文档（JDK-8）

## 1. API 功能

所有通道的根接口：isOpen()/close()，extends Closeable（进而 AutoCloseable）。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：面向 Channel 编程做统一关闭管理（try-with-resources），具体 IO 经 ReadableByteChannel/
  WritableByteChannel 等子面。
- 组合用法：与 Selector（select 后 cancelled 集合）、Files.newByteChannel、Pipe 两端、Socket/Datagram/
  File 通道配合。
- 生命周期：open -> ... -> close 幂等；关闭后再 IO 由实现抛 ClosedChannelException。
- 失败路径：close 声明 throws IOException；实现类以精确子类（AsynchronousCloseException 等）细化。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- Netty/Vert.x 类框架以 Channel 抽象资源生命周期，业务侧 try-with-resources 批量收尾；
- 用法特点：常作泛型约束 `<C extends Channel>` 与集合收尾；测试侧临时目录/回环端口 + finally 关闭。

## 4. 测试用例设计 + 自动化实现

文件：`ChannelTest.java`（单文件，基线 testChannel() + A5 新增 testWhiteBoxA5()/testBlackBoxA5()，由 main 依次调用）。

设计：白盒校验接口面恰为 isOpen+close 两方法、无 default 方法（实现者必须全供）、close 声明
throws IOException、继承 AutoCloseable、无读方向；黑盒用最小自定义实现 FlagChannel 钉死
try-with-resources 恰好关闭一次 + 二次关闭被实现容忍；再以 FileChannel（含 newByteChannel）、
DatagramChannel、Pipe 两端做真实通道的 isOpen/close/IO-through-faces 验证。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件用例 `Files.createTempDirectory` + finally 删除；Pipe 两端 finally 关闭。
- headless 安全，不加 @key；JDK-8 侧断言数：4→25 条。
