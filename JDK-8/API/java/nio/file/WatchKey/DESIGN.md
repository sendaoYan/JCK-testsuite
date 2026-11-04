# java.nio.file.WatchKey 设计文档

## 1. API 功能

监视键接口: isValid/cancel/pollEvents/reset/watchable。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: Path.register(WatchService, kinds...) 得到 WatchKey -> 事件到达 -> pollEvents 消费 -> reset 复用。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- WatchKey
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`WatchKeyTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口/五方法；黑盒验证新键有效、watchable 指向目录、事件一次取出后清空、reset 复用、cancel 失效、重注册产生新键、服务关闭使键失效。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：19 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：key 同一性（同 service 重复注册同目录返回同一 WatchKey 对象——LinuxWatchService 实测；不同 service 各自 key 且各自收到事件）；cancel 语义（幂等、cancel 后 watchable() 保留、pollEvents 空、reset false、取消后不再收事件 poll(2s)=null）；重新注册产生新 key 且只收新事件；closed service 的 key（isValid false、pollEvents 空不抛、reset false、cancel 容忍、watchable 保留）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。
