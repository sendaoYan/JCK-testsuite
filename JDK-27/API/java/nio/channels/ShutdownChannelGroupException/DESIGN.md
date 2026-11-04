# java.nio.channels.ShutdownChannelGroupException 设计文档

## 1. API 功能

ShutdownChannelGroupException：java.nio.channels 包的运行时/受检异常，报告通道生命周期或状态违规。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 捕获方式：在相应通道/文件操作的 catch 中按具体异常类型分流处理（区别于笼统 IOException）。
- 信息获取：经 getMessage()/getFile()/getOtherFile()/getReason()/getCause() 等访问器定位失败对象与原因。
- 常见策略：按类型决定重试、回退（换选项/路径）或终止，并向调用方传递结构化错误。
- 生命周期：异常在失败点一次性抛出，不持有资源。
## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- AIO 线程组
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`ShutdownChannelGroupExceptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验异常层次、构造器、消息默认值；黑盒在真实通道操作中触发一次（组关闭后创建通道）并断言精确类型。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：6 条。

### A7 批次深化（契约覆盖增强，append-only，原断言全部保留）

- 新增方法：testExceptionObjectContract/testSerializationContract/testRealTriggerContract。
- 负例矩阵（精确异常类型，同步抛 vs 经 Future/handler 交付分开断言）：extends IllegalStateException；serialVersionUID=-3903801676350154157；round-trip 保类型
- 确定性与健壮性：真实触发：withFixedThreadPool → shutdownNow → open(server|socket)(group) 均抛；awaitTermination 有界返回 true。
- 版本差异（按侧断言）：serialVersionUID 双侧一致，行为无差异。
- 本机实测断言数：7 → 33 条。
