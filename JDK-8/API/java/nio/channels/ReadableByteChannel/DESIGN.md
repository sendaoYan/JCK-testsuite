# java.nio.channels.ReadableByteChannel 设计文档

## 1. API 功能

可读通道接口: read(ByteBuffer)；继承 Channel。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: 流式读取方抽象。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- ReadableByteChannel
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`ReadableByteChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口、read 存在且无 write；黑盒验证 read 分批读取、EOF 返回 -1 且缓冲不动、只读缓冲抛 IllegalArgumentException、关闭后读抛 ClosedChannelException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力以运行时能力守护，不支持时跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：12 条。

### 4.1 A5 契约深化追加

新增 ArrayChannel 最小实现（顺序交付、n 语义、[position,limit) 遵守、满缓冲 0、关闭后 CCE）、
testWhiteBoxA5（单声明方法/throws IOException/无 default/无写向）、testFileChannelView（偏移缓冲、
只读目标缓冲 IAE（实测）、空缓冲 0）。断言数 12→33 条（JDK-8 实测）。
