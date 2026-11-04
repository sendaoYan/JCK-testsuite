# java.nio.channels.FileLock 设计文档（JDK-8）

## 1. API 功能

文件锁令牌（代表 Java 平台文件锁，非进程级建议锁）：channel()/acquiredBy()/position()/size()/isShared()/
overlaps()/isValid()/release()。

（签名以 jdk8u372 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：`FileChannel.lock()/tryLock()` 取得令牌，持锁期间读写，finally 中 release()；
  单 JVM 内同一文件重叠区域互斥，跨进程由 OS 文件锁仲裁。
- 组合用法：lock + try-with-resources（FileLock 仅在 JDK-19+ 实现 AutoCloseable，JDK-8 需显式 release）；
  与 transferTo/map 组合时注意锁不阻止同 JVM 其它通道访问。
- 生命周期：acquired -> valid -> release/close 后 isValid()==false；release 幂等；通道关闭后 release 抛
  ClosedChannelException。
- 失败路径：重叠区域已持锁 -> OverlappingFileLockException；共享锁需可读通道 -> NonReadableChannelException；
  独占锁需可写通道 -> NonWritableChannelException。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 数据库单实例守卫（H2/Derby 类 .lock 文件）、构建缓存互斥、目录锁；
- 用法特点：tryLock 非阻塞探测 + 失败退出是主流；测试侧临时目录 + finally release + 删除。

## 4. 测试用例设计 + 自动化实现

文件：`FileLockTest.java`（单文件，基线 testFileLock() + A5 新增 StubLock 白盒/黑盒、testRealLocks 矩阵，由 main 依次调用）。

设计：白盒校验抽象类/2 个受保护构造器/final 方法面（channel/position/size/isShared/overlaps/close/toString）/
isValid+release 抽象；StubLock 黑盒钉死 JDK-8 语义：null channel 构造被接受、负 position/size -> IAE；
testRealLocks 覆盖重叠矩阵（含 JDK-8 零长锁查询 (0,0)/(10,0) 为 false 的字面语义）、release 幂等、
同 JVM 跨通道 OFLE、共享锁在 R/W 通道可用、共享锁需读权限、越界锁、全文件 (0, Long.MAX_VALUE) 锁。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 全部 `Files.createTempDirectory` + finally release/删除；锁在用例内即时释放避免相互影响。
- headless 安全，不加 @key；JDK-8 侧断言数：9→84 条（零长锁等版本差异与 JDK-27 侧各自断言）。
