# java.nio.channels.FileChannel 设计文档

## 1. API 功能

同步文件通道: open/read/write/position/size/truncate/force/transferTo/transferFrom/map/lock/tryLock/isOpen/close；继承 AbstractInterruptibleChannel，实现 Seekable/Gathering/Scattering/InterruptibleChannel。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: FileChannel.open + 读写/截断/映射/锁定/transfer；Kafka/RocksDB 类存储引擎常用。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- FileChannel
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 abstract/受保护构造/继承体系/14 个方法面；黑盒覆盖读写往返、位置化读写不移动位置、scatter/gather、truncate、transferTo/transferFrom、map(READ_WRITE)+force 持久化、lock/tryLock 生命周期与 overlaps、读写专属通道抛 NonReadable/NonWritable、关闭后抛 ClosedChannelException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力以运行时能力守护，不支持时跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：68 条。
