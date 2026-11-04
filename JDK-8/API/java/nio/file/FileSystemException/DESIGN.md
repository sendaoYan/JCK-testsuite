# java.nio.file.FileSystemException 设计文档

## 1. API 功能

文件系统异常基类 (extends IOException): 携带 file/otherFile/reason 三个字段，构造器 (String)/(String,String,String)。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: catch 该异常或其子类并读取 getFile/getOtherFile/getReason 定位失败路径。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- FileSystemException
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileSystemExceptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验继承 IOException、getFile/getOtherFile/getReason、单参构造置空；黑盒验证真实 NoSuchFileException 携带路径、消息组合 file/other/reason、单文件消息。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：9 条。


### 第 4 部分补充（批次 A11 契约深化）

在既有用例之上新增 testContract* 系列（构造矩阵 / getMessage-null 分支矩阵 / initCause 一次性 / serialVersionUID -3055425747967319812 + round-trip / 真实触发 / 精确负例）：

- 真实触发：真实 NoSuchFileException（readAttributes 缺失文件）作载体断言三 getter/消息。
- 断言计数更新（JDK-8 侧 / JDK-27 侧）：71 / 71 条（`PASS (N assertions)` 实测）。
- 健壮性：临时目录 + finally 递归清理；权限用例 finally 恢复；能力守护（root/跨 store/zip provider）NOTE 跳过不判失败。
