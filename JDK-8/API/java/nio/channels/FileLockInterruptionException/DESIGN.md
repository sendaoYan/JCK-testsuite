# java.nio.channels.FileLockInterruptionException 设计文档（JDK-8）

## 1. API 功能

可中断通道获取文件锁时被中断所抛的受检异常（InterruptibleChannel 契约的一部分）。

（签名以 jdk8u372 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：在 FileChannel.lock() 等可阻塞调用外围 catch，配合 Thread.interrupt() 实现"打断等锁"。
- 组合用法：捕获后通道已被关闭（InterruptibleChannel 契约：中断即关闭通道），后续 IO 抛
  ClosedChannelException；中断状态位仍置位（Thread.interrupted() 可观测）。
- 失败路径：本异常自身即失败报告；无 cause（构造时不接受 cause，initCause 幂等失败）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 需要可取消锁等待的数据库/缓存组件：等待线程 interrupt() + 调用方 catch 后重建通道；
- 用法特点：常与 AsynchronousCloseException 族并列处理；测试侧以自中断 + 单线程确定性触发，
  避免跨线程时序抖动。

## 4. 测试用例设计 + 自动化实现

文件：`FileLockInterruptionExceptionTest.java`（单文件，基线 + A5 深化，由 main 依次调用）。

设计：白盒校验类型层级（extends IOException）、serialVersionUID==7104080643653532383L、
序列化 round-trip、initCause 二次调用抛 IllegalStateException；黑盒用"自中断 + lock()"确定性触发：
抛出本异常、通道已被中断关闭（isOpen()==false）、中断标志保持置位（Thread.interrupted() 观测后清除）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 触发用例排在持锁断言之后，避免通道提前关闭影响其它断言；临时目录 + finally 删除。
- headless 安全，不加 @key；JDK-8 侧断言数：4→19 条（与 JDK-27 侧行为一致，双侧各自断言）。
