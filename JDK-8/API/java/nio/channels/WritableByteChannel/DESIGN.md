# java.nio.channels.WritableByteChannel 设计文档

## 1. API 功能

可写通道接口: write(ByteBuffer)；继承 Channel。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: 流式写入方抽象。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- WritableByteChannel
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`WritableByteChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口、write 存在且无 read；黑盒验证 write 消耗缓冲、APPEND 追加、关闭后写抛 ClosedChannelException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力以运行时能力守护，不支持时跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：11 条。

### 4.1 A5 契约深化追加

新增 SinkChannel 最小实现（n 返回值、[position,limit) 遵守、空/满缓冲 0、关闭后 CCE）、testWhiteBoxA5
（单声明方法/throws IOException/无 default/无读向）、testFileChannelView（切片写只落切片字节、空写不
扩展文件）。断言数 11→31 条（JDK-8 实测）。
