# jdk.nio.Channels.SelectableChannelCloser 契约设计

## 1. API 功能
`jdk.nio.Channels.SelectableChannelCloser` 是嵌套在 `jdk.nio.Channels` 中的回调接口（模块 jdk.net，JDK 16+ 移植自 jdk.sctp 的 fd 复用机制）。它把"文件描述符的所有权"从通道生命周期中剥离：通道 close 时回调 `implCloseChannel(SelectableChannel)`，而 `implReleaseChannel(SelectableChannel)` 仅在底层 fd 被真正释放（通道对象本身已被 GC 或显式弃用）时回调。两个方法均声明 `throws IOException`、接收同一个通道实例。

## 2. Java 用户如何使用
调用方（如 AFD socket 通道、foreign-memory 支撑通道的宿主库）在 `Channels.readWriteSelectableChannel(fd, closer)` 时传入实现，把 fd 借给 NIO 选择器体系；选择器注册、就绪集合照常工作，close/re release 两个时机由宿主库自行决定是否真正 `close(2)` 底层 fd。匿名内部类是最常见形态。

## 3. 开源代码实际用法调研
- OpenJDK jdk.net 源码：`jdk.nio.Channels.readWriteSelectableChannel` 是唯一工厂，closer 恒被要求非空（null 直接 NPE）。
- OpenJDK jdk.incubator.concurrent / StructuredConcurrency 早期原型用它支撑"选择器可注册但 fd 由外部持有"的通道。
- Netty、Aeron 等库不直接使用该内部 API，而是复刻同样的"fd 借用 + 双阶段释放"模式（epoll 事件循环里 detach fd 与 close fd 分离）。

## 4. 测试用例设计+自动化实现
- 反射验证精确方法面：恰好 `implCloseChannel`/`implReleaseChannel` 两个方法，均 public abstract、单参 `java.nio.channels.SelectableChannel`、唯一受检异常 IOException、无任何常量字段。
- 白盒结构：public 成员类、隶属 jdk.nio.Channels。
- 行为闭环（testDeepen）：借真实文件 fd 创建通道，close 两次，断言 `implCloseChannel` 恰好调用 1 次（幂等），与 ChannelsTest 中"implReleaseChannel 不被 close 触发"互补。
- 自动化：jtreg `run main`，断言计数 `check()`，全 headless 无窗口依赖。
