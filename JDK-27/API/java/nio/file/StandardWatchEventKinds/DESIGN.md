# java.nio.file.StandardWatchEventKinds 设计文档

## 1. API 功能

标准事件种类常量：ENTRY_CREATE/DELETE/MODIFY/OVERFLOW。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `StandardWatchEventKinds` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 目录监视
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`StandardWatchEventKindsTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验名称与 type；黑盒注册真实监视并验证三类事件送达与 Path 上下文。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：14 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：kind 矩阵（entry 三者 type Path、OVERFLOW type Object 实测、toString==name、引用相等矩阵、HashSet size=4、同名自制 Kind 不等）；容器白盒（final/私有构造/字段>=4）；inotify 实测：注册三事件→写新文件→有界 poll 分批重试至 CREATE+MODIFY 均见（JDK-27 实测 MODIFY 可能晚于首批到达，重试吸收）→context 精确/count==1→reset→delete→ENTRY_DELETE。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。
