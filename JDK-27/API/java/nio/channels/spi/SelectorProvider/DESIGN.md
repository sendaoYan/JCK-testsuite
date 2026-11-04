# java.nio.channels.spi.SelectorProvider 设计文档

## 1. API 功能

选择器与通道工厂：provider() 单例、open* 工厂、inheritedChannel。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `SelectorProvider` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 框架自定 provider（Netty epoll 模式）
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SelectorProviderTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验抽象与保护构造器；黑盒验证 provider() 稳定、各 open 产物绑定同一 provider、协议族重载、inheritedChannel 为 null。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：25 条。

### A7 批次深化（契约覆盖增强，append-only，原断言全部保留）

- 新增方法：testProviderSurfaceContract/testOpenContract/testProtocolFamilyOverloadsContract。
- 负例矩阵（精确异常类型，同步抛 vs 经 Future/handler 交付分开断言）：provider() 缓存单例；openDatagramChannel(null) 抛 NullPointerException；两次 open 各自独立；inheritedChannel null 或 open 通道（环境相关，柔性断言）；JDK-27：openDatagramChannel(UNIX) 抛 UnsupportedOperationException + UNIX 域全双工 round-trip（bind/connect/accept/写读 1 字节）
- 确定性与健壮性：JDK-8 侧断言 openSocketChannel(ProtocolFamily)/openServerSocketChannel(ProtocolFamily) 与 StandardProtocolFamily.UNIX 常量均不存在。
- 版本差异（按侧断言）：JDK-16+ 的 ProtocolFamily 流通道重载仅 JDK-27 存在（双侧反射断言）；INET6 数据报在本机 JDK-8 抛 UnsupportedOperationException、JDK-27 可用（环境差异，不做硬断言）；inheritedChannel 双侧均非 null（JDK-8 为 server-socket 形态、JDK-27 为 socket 形态，环境相关故柔性断言）。
- 本机实测断言数：20 → 58 条。
