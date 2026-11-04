# java.nio.channels.ByteChannel 设计文档

## 1. API 功能

可读可写通道聚合接口，同时继承 ReadableByteChannel 与 WritableByteChannel。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: 双工通道统一抽象。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- ByteChannel
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`ByteChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口继承关系与 read/write 方法存在；黑盒经 FileChannel 视图验证同一通道双向读写与覆盖。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力以运行时能力守护，不支持时跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：9 条。
