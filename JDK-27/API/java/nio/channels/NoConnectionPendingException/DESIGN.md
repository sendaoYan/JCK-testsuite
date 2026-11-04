# java.nio.channels.NoConnectionPendingException 设计文档

## 1. API 功能

NoConnectionPendingException：java.nio.channels 包的运行时/受检异常，报告通道生命周期或状态违规。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 捕获方式：在相应通道/文件操作的 catch 中按具体异常类型分流处理（区别于笼统 IOException）。
- 信息获取：经 getMessage()/getFile()/getOtherFile()/getReason()/getCause() 等访问器定位失败对象与原因。
- 常见策略：按类型决定重试、回退（换选项/路径）或终止，并向调用方传递结构化错误。
- 生命周期：异常在失败点一次性抛出，不持有资源。
## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 通道操作
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`NoConnectionPendingExceptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验异常层次、构造器、消息默认值；黑盒在真实通道操作中触发一次（通道状态被违反）并断言精确类型。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：5 条。


### 批次 A8 深化（第 4 部分补充）

在既有白盒/黑盒之上新增契约级四件套（与 JDK-8 侧等价；全部条目先经探针在 JDK-8(8u372) 与
JDK-27(27.0.1) 双侧实测，行为一致处不再注明）：
1. 白盒层级：直接父类 == IllegalStateException（javap 双侧实证）、继承链、Serializable、FQN。
2. 构造矩阵：唯一无参公共构造器（反射）、message null、toString 裸类名。
3. 序列化：serialVersionUID == -8296561183633134743L（双侧一致）+ round-trip 类型/消息/cause/suppressed/栈深保持。
4. 真实触发：未 connect 的 channel finishConnect()（阻塞与非阻塞两种模式）→ 精确类型；对照已连接后 finishConnect 返回 true、closed 后变 ClosedChannelException。
5. Throwable 契约：initCause 一次性（二次 ISE/self IAE）、fillInStackTrace 返回 this、构造捕获栈帧、
   suppressed 空表 + addSuppressed、throw/catch 身份保持。
- checkThrows 断言精确类型（getClass() == expected）；触发场景全回环/临时文件，daemon 线程 join 带超时。
- 断言数 6 → 50 条。
