# java.nio.channels.spi.AbstractSelectionKey 设计文档

## 1. API 功能

选择键骨架：isValid/invalidate/cancel 生命周期 + attach/attachment 附件语义。

（签名以 JDK-8 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AbstractSelectionKey` 编程，或经 Channels/Selector/Files 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（provider+group+channel、selector+key+channel、handler+Future 双形式）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources 或 finally close。
- 失败路径：通过本类型或配套异常类型精确报告失败（见第 4 节负例矩阵）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 自定义 selector 生态的 key 骨架
- 用法特点：优先走高层门面，仅在需要精细控制（挂起态、超时、中断、自定义 provider）时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AbstractSelectionKeyTest.java`（单文件；既有方法 + A7 批次新增方法，由 main 依次调用）。

原设计（既有断言 3 条）：结构面（abstract/继承关系/构造器可见性/provider() 单例与 open* 冒烟）基础校验。

A7 批次深化（append-only，原断言全部保留）：
- 新增方法：testStructureContract/testValidInvalidateContract/testAttachContract/testRealKeyContract。
- 负例矩阵（精确异常类型，同步抛 vs 异步交付分开断言）：新 key valid；cancel 立即失效且幂等；attach 返回前值、attach(null) 清空、identity 保持；真实键 interestOps 越界抛 IllegalArgumentException、interestOps(int) 返回自身、通道 close 使 key 失效
- 确定性与健壮性：OP_READ/OP_WRITE/OP_CONNECT/OP_ACCEPT 常量值 1/4/8/16 断言。
- 版本差异处理：无行为差异。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 套接字用例仅用本地回环地址与临时端口；线程全部 daemon；异步等待全部 latch/Future.get(超时) 有界；
  临时文件/目录 finally 递归清理；组全部 shutdown。
- 本机实测断言数：35 条。
