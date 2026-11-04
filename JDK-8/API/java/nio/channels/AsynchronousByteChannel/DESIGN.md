# java.nio.channels.AsynchronousByteChannel 设计文档

## 1. API 功能

异步字节通道接口：read/write 各两个重载（Future 形式与 (ByteBuffer,A,CompletionHandler) 形式）。

（签名以 JDK-8 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AsynchronousByteChannel` 编程，或经 Channels/Selector/Files 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（provider+group+channel、selector+key+channel、handler+Future 双形式）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources 或 finally close。
- 失败路径：通过本类型或配套异常类型精确报告失败（见第 4 节负例矩阵）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 面向字节流抽象的异步 IO 接口层
- 用法特点：优先走高层门面，仅在需要精细控制（挂起态、超时、中断、自定义 provider）时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AsynchronousByteChannelTest.java`（单文件；既有方法 + A7 批次新增方法，由 main 依次调用）。

原设计（既有断言 4 条）：结构面（abstract/继承关系/构造器可见性/provider() 单例与 open* 冒烟）基础校验。

A7 批次深化（append-only，原断言全部保留）：
- 新增方法：testSignatureContract/testBehaviourThroughInterface。
- 负例矩阵（精确异常类型，同步抛 vs 异步交付分开断言）：getDeclaredMethods 恰 4 个且全部抽象；接口面读写/关闭语义与具体通道一致；AsynchronousFileChannel（positioned）与同步 SocketChannel 不实现本接口
- 确定性与健壮性：以 AsynchronousSocketChannel 为实现，经接口引用完成 Future/handler 双形式读写与 close。
- 版本差异处理：无版本差异（方法面双侧一致）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 套接字用例仅用本地回环地址与临时端口；线程全部 daemon；异步等待全部 latch/Future.get(超时) 有界；
  临时文件/目录 finally 递归清理；组全部 shutdown。
- 本机实测断言数：27 条。
