# java.nio.file.NoSuchFileException 设计文档

## 1. API 功能

文件不存在异常 (extends FileSystemException): 构造器 (String) 与 (String,String,String)。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: 读取/复制缺失路径时捕获，getFile 定位缺失文件。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- NoSuchFileException
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`NoSuchFileExceptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验继承 FileSystemException、file/other 存取；黑盒验证 readAllBytes 缺失抛该异常、copy 缺失源抛该异常、deleteIfExists 静默。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：5 条。
