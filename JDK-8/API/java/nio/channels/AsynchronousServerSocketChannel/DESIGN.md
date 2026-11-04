# java.nio.channels.AsynchronousServerSocketChannel 设计文档

## 1. API 功能

异步服务端侦听通道：bind/accept（Future 与 CompletionHandler 两种形式）、组绑定与选项。

（签名以 JDK-8 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AsynchronousServerSocketChannel` 编程，或经 Channels/Selector/Files 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（provider+group+channel、selector+key+channel、handler+Future 双形式）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources 或 finally close。
- 失败路径：通过本类型或配套异常类型精确报告失败（见第 4 节负例矩阵）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- AIO 服务端 acceptor（少量连接高吞吐场景）
- 用法特点：优先走高层门面，仅在需要精细控制（挂起态、超时、中断、自定义 provider）时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AsynchronousServerSocketChannelTest.java`（单文件；既有方法 + A7 批次新增方法，由 main 依次调用）。

原设计（既有断言 5 条）：结构面（abstract/继承关系/构造器可见性/provider() 单例与 open* 冒烟）基础校验。

A7 批次深化（append-only，原断言全部保留）：
- 新增方法：testUnboundAndBindContract/testAcceptContract/testGroupAndSurfaceContract。
- 负例矩阵（精确异常类型，同步抛 vs 异步交付分开断言）：NotYetBoundException（未 bind 即 accept）、AcceptPendingException（挂起 accept + 二次 accept，Future 与 handler 两种形式）、AlreadyBoundException、bind(null) 自动分配、backlog 语义
- 确定性与健壮性：close 终止挂起 accept → Future.get 抛 ExecutionException，cause 精确为 AsynchronousCloseException（双侧实测）；accept 无超时重载（API 面断言）。
- 版本差异处理：close 后再 accept 的失败经 Future 交付（非同步抛）；accept 无 timeout 重载。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 套接字用例仅用本地回环地址与临时端口；线程全部 daemon；异步等待全部 latch/Future.get(超时) 有界；
  临时文件/目录 finally 递归清理；组全部 shutdown。
- 本机实测断言数：48 条。
