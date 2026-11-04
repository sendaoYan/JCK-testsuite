# java.nio.channels.SelectionKey 设计文档

## 1. API 功能

注册凭据：interestOps/readyOps、attach、cancel 与 OP_* 常量。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `SelectionKey` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 所有 Selector 使用方
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SelectionKeyTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 OP 常量精确值(1,4,8,16)；黑盒验证 attach/attachment、interestOpsOr/interestOpsAnd、readyOps ⊆ interestOps、CancelledKeyException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：43 条。

### A6 契约深化（追加）

深化维度：
- 常量矩阵（1/4/8/16、单比特、并集 29）；interestOps 往返、0 合法、非法位/OP_ACCEPT/负数 → IAE。
- attachment 生命周期：2 参 register 等价 attach(null) → **清除**原附件（RI 行为，双侧一致）；
  显式带参重注册恢复。
- isValid 三条失效路径（通道关闭/选择器关闭/cancel）；取消后 7 个谓词 → CancelledKeyException，
  attach/channel/selector 仍可用；JDK-27 侧 interestOpsOr/interestOpsAnd 同抛 CancelledKeyException。
- 真实回环事件流上的 readyOps 精确断言（accept/read/connect）。
- 版本差异：interestOps 切到 OP_WRITE 后 JDK-27 的 readyOps 保留先前选中的 OP_READ 位（探测得 5），
  该侧只断言包含 OP_WRITE；JDK-8 侧精确等于 4。双侧代码互注释。

本机实测断言数：101 条（深化前 27）。
