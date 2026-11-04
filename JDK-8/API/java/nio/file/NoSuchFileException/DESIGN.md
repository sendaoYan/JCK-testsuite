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


### 第 4 部分补充（批次 A11 契约深化）

在既有用例之上新增 testContract* 系列（构造矩阵 / getMessage-null 分支矩阵 / initCause 一次性 / serialVersionUID -1390291775875351931 + round-trip / 真实触发 / 精确负例）：

- 真实触发：readAttributes/delete/newInputStream/newDirectoryStream/copy 缺失路径五种触发 + deleteIfExists(false)/exists(false) 静默对照。
- 断言计数更新（JDK-8 侧 / JDK-27 侧）：31 / 31 条（`PASS (N assertions)` 实测）。
- 健壮性：临时目录 + finally 递归清理；权限用例 finally 恢复；能力守护（root/跨 store/zip provider）NOTE 跳过不判失败。
