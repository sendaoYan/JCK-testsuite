# java.nio.channels.MembershipKey 设计文档

## 1. API 功能

组播成员凭据：group/networkInterface/sourceAddress/block/unblock/drop。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `MembershipKey` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 组播实现
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`MembershipKeyTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口方法；黑盒验证 join 元数据（sourceAddress null）、二次 drop 无害、block 返回 this、unblock 未阻塞抛 IllegalStateException、close 使 key 失效。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：22 条。

### A6 契约深化（追加）

深化维度：
- 生命周期：join/drop 幂等、getter 在 drop 后仍可读（仅 isValid 翻转）、重 join 新 key、
  通道关闭失效、关闭后 join → ClosedChannelException。
- block 契约：返回 this、**重复 block 幂等 no-op（RI 偏离 javadoc 的 ISE，以实测为准并注释）**、
  unblock 未 block → ISE、block(null) → NPE、block(组播地址) → IAE、drop 后 block/unblock → ISE。
- 特定源成员：sourceAddress 记录、重 join 幂等、特定源上 block/unblock → ISE、
  任意源在场时特定源 join → ISE。
- 真实源过滤数据面：block 发送网卡地址后 select(1s)==0（被滤除）→ unblock 恢复送达。

本机实测断言数：46 条（深化前 16）。
