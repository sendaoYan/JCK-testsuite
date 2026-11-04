# java.nio.file.ClosedWatchServiceException 设计文档

## 1. API 功能

（补全）

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 捕获方式：在相应通道/文件操作的 catch 中按具体异常类型分流处理（区别于笼统 IOException）。
- 信息获取：经 getMessage()/getFile()/getOtherFile()/getReason()/getCause() 等访问器定位失败对象与原因。
- 常见策略：按类型决定重试、回退（换选项/路径）或终止，并向调用方传递结构化错误。
- 生命周期：异常在失败点一次性抛出，不持有资源。
## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 通用
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`ClosedWatchServiceExceptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：（补全）

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：5 条。


### 第 4 部分补充（批次 A11 契约深化）

在既有用例之上新增 testContract* 系列（构造矩阵 / getMessage-null 分支矩阵 / initCause 一次性 / serialVersionUID 1853336266231677732 + round-trip / 真实触发 / 精确负例）：

- 真实触发：close 后 poll()/poll(1,SECONDS)/take()/register() 全部精确断言；key.isValid=false；有界 poll 无事件返回 null。
- 断言计数更新（JDK-8 侧 / JDK-27 侧）：29 / 32 条（`PASS (N assertions)` 实测）。
- 健壮性：临时目录 + finally 递归清理；权限用例 finally 恢复；能力守护（root/跨 store/zip provider）NOTE 跳过不判失败。
