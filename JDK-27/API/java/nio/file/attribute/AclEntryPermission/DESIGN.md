# java.nio.file.attribute.AclEntryPermission 设计文档

## 1. API 功能

ACL 权限 14 常量 + JDK-8334333 三个别名字段（LIST_DIRECTORY/ADD_FILE/ADD_SUBDIRECTORY）。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AclEntryPermission` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- NFS4 ACL
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AclEntryPermissionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 14 常量与别名恒等（READ_DATA/WRITE_DATA/APPEND_DATA）；黑盒验证 values 长度、顺序、经 AclEntry 往返、EnumSet 自然排序。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：41 条。

### 第 4 部分补充（批次 A12 契约深化）

在既有白盒+黑盒基础上追加 `testContract()`（及 FileAttribute 的 `testContract2()`），契约维度扩至：
- values() 恰 14 顺序精确 + 逐常量 name/ordinal/valueOf + valueOf 负例 + EnumSet
  + Builder 集成（单条目携带全部 14 权限）。

健壮性：新增用例仍走临时目录 + finally 递归清理；权限/属主/DOS 位修改类断言先恢复再删；
负例用 checkThrows/checkThrowsIO 断言精确异常类型（IO 路径用 IORunnable 变体）；
平台实测事实（Linux ext4：acl 视图不可用、dos/user 视图可用、dos 属性经 user.DOSATTRIB xattr
模拟且 readonly 不被内核强制、creationTime 为模拟值、ctime 不可设置）在断言处注释。
JDK-27 侧实测断言数：91 条。
