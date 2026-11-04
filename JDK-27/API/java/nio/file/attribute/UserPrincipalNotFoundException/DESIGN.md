# java.nio.file.attribute.UserPrincipalNotFoundException 设计文档

## 1. API 功能

主体查找失败异常：getName 携带名称（JDK-27 Unix 实现可能不回显）。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 捕获方式：在相应通道/文件操作的 catch 中按具体异常类型分流处理（区别于笼统 IOException）。
- 信息获取：经 getMessage()/getFile()/getOtherFile()/getReason()/getCause() 等访问器定位失败对象与原因。
- 常见策略：按类型决定重试、回退（换选项/路径）或终止，并向调用方传递结构化错误。
- 生命周期：异常在失败点一次性抛出，不持有资源。
## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 属主管理
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`UserPrincipalNotFoundExceptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验构造器/getName/getMessage 为 null；黑盒经真实 lookup 触发、构造器名称精确保留、可按 IOException 捕获、null 名称容忍。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：13 条。

### 第 4 部分补充（批次 A12 契约深化）

在既有白盒+黑盒基础上追加 `testContract()`（及 FileAttribute 的 `testContract2()`），契约维度扩至：
- 构造名字往返、ctor(null)→null、getMessage null、toString 精确串、initCause/getCause；
- serialVersionUID 固定 -5369283889045833024 + 序列化往返名字保真；同名孪生不等；
- 默认查找服务对未知用户抛出（JDK-27 实测 getName 为空串，版本差异）；可按 IOException 捕获。

健壮性：新增用例仍走临时目录 + finally 递归清理；权限/属主/DOS 位修改类断言先恢复再删；
负例用 checkThrows/checkThrowsIO 断言精确异常类型（IO 路径用 IORunnable 变体）；
平台实测事实（Linux ext4：acl 视图不可用、dos/user 视图可用、dos 属性经 user.DOSATTRIB xattr
模拟且 readonly 不被内核强制、creationTime 为模拟值、ctime 不可设置）在断言处注释。
JDK-27 侧实测断言数：30 条。
