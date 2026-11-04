# java.nio.channels.spi.SelectorProvider 设计文档

## 1. API 功能

同步 provider SPI：openSelector/openPipe/open*(Datagram)Channel/inheritedChannel 工厂族。

（签名以 JDK-8 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `SelectorProvider` 编程，或经 Channels/Selector/Files 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（provider+group+channel、selector+key+channel、handler+Future 双形式）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources 或 finally close。
- 失败路径：通过本类型或配套异常类型精确报告失败（见第 4 节负例矩阵）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 替换默认 NIO 实现的 SPI（epoll/poll 选择、测试桩、Unix 域支持探测）
- 用法特点：优先走高层门面，仅在需要精细控制（挂起态、超时、中断、自定义 provider）时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SelectorProviderTest.java`（单文件；既有方法 + A7 批次新增方法，由 main 依次调用）。

原设计（既有断言 8 条）：结构面（abstract/继承关系/构造器可见性/provider() 单例与 open* 冒烟）基础校验。

A7 批次深化（append-only，原断言全部保留）：
- 新增方法：testProviderSurfaceContract/testOpenContract/testProtocolFamilyOverloadsContract。
- 负例矩阵（精确异常类型，同步抛 vs 异步交付分开断言）：provider() 缓存单例；openDatagramChannel(null) 抛 NullPointerException；两次 open 各自独立；inheritedChannel null 或 open 通道（环境相关，柔性断言）；JDK-27：openDatagramChannel(UNIX) 抛 UnsupportedOperationException + UNIX 域全双工 round-trip（bind/connect/accept/写读 1 字节）
- 确定性与健壮性：JDK-8 侧断言 openSocketChannel(ProtocolFamily)/openServerSocketChannel(ProtocolFamily) 与 StandardProtocolFamily.UNIX 常量均不存在。
- 版本差异处理：JDK-16+ 的 ProtocolFamily 流通道重载仅 JDK-27 存在（双侧反射断言）；INET6 数据报在本机 JDK-8 抛 UnsupportedOperationException、JDK-27 可用（环境差异，不做硬断言）；inheritedChannel 双侧均非 null（JDK-8 为 server-socket 形态、JDK-27 为 socket 形态，环境相关故柔性断言）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 套接字用例仅用本地回环地址与临时端口；线程全部 daemon；异步等待全部 latch/Future.get(超时) 有界；
  临时文件/目录 finally 递归清理；组全部 shutdown。
- 本机实测断言数：35 条。
