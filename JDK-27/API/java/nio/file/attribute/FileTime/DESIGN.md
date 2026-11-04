# java.nio.file.attribute.FileTime 设计文档

## 1. API 功能

文件时间戳：fromMillis/from(Instant)/from(long,unit)/to/toInstant/compareTo/toString。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `FileTime` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 文件时间处理
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileTimeTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验工厂与比较器；黑盒验证固定 Instant 精确往返（毫秒截断/纳秒保留）、toString 格式（整秒/分数秒）、负时间、Files.setLastModifiedTime 往返。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：34 条。

### 第 4 部分补充（批次 A12 契约深化）

在既有白盒+黑盒基础上追加 `testContract()`（及 FileAttribute 的 `testContract2()`），契约维度扩至：
- fromMillis 往返矩阵（0/负/Long.MAX_VALUE/Long.MIN_VALUE）；to(TimeUnit) 七单位换算
  （截断向零、饱和：MAX 毫秒→微/纳秒饱和，纳秒往返损失精度实测 9223372036854）；
- 跨表示 equals（1000ms==1s）、亚毫秒 compareTo（999ns<1ms<…、1000000ns==1ms）；
- toInstant 往返；toString 14 个精确串（epoch/纪元前/三种小数位/0001/9999 年/远未来/远过去）；
- FileTime 非 Serializable + ObjectOutputStream 拒绝；Files mtime 往返含纪元前。

健壮性：新增用例仍走临时目录 + finally 递归清理；权限/属主/DOS 位修改类断言先恢复再删；
负例用 checkThrows/checkThrowsIO 断言精确异常类型（IO 路径用 IORunnable 变体）；
平台实测事实（Linux ext4：acl 视图不可用、dos/user 视图可用、dos 属性经 user.DOSATTRIB xattr
模拟且 readonly 不被内核强制、creationTime 为模拟值、ctime 不可设置）在断言处注释。
JDK-27 侧实测断言数：94 条。
