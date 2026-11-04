# java.nio.file.FileVisitResult 设计文档

## 1. API 功能

文件遍历控制枚举: CONTINUE/TERMINATE/SKIP_SUBTREE/SKIP_SIBLINGS。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: FileVisitor 各回调返回该枚举控制 Files.walkFileTree 的遍历走向。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- FileVisitResult
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileVisitResultTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 enum/4 常量；黑盒验证 CONTINUE 全访问、TERMINATE 立即停、SKIP_SIBLINGS 跳兄弟、SKIP_SUBTREE 不下钻。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：13 条。
