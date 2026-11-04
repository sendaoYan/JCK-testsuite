# java.nio.file.attribute.PosixFilePermissions 设计文档

## 1. API 功能

rwx 权限串与 Set<PosixFilePermission> 的编解码器 + asFileAttribute(Set) 适配器：
fromString(String)、toString(Set)、asFileAttribute(Set)（name 固定 "posix:permissions"，
value 为构造时快照拷贝）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

chmod 语义程序化：PosixFilePermissions.fromString("rwxr-x---") 得权限集，
asFileAttribute 后传入 Files.createFile/createDirectory 精确设定初始模式（不经 umask）；
与 PosixFileAttributeView.setPermissions 配合读写模式。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/PosixFileAttributeView/Basic.java：setPermissions/fromString/toString
  往返与 readAttributes 对照；
- sun.nio.fs.UnixFileMode/UnixFileAttributeViews：模式位与字符串互转实现；
- Files.createFile 侧 FileAttribute 消费路径。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`PosixFilePermissionsTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：
- fromString 精确成员矩阵（rwxrwxrwx 全 9、--------- 空、rwxr-x---、--xr-x--x）；
- 9 槽位逐位灵敏度：每槽单字符解析为唯一对应常量；错字符（槽位不匹配）逐槽 IAE；
- 6 个真实模式 toString 往返；
- 非法矩阵：null→NPE；""/短/长/非法字符/大写/双 x/尾随空格→IllegalArgumentException 且消息精确为 "Invalid mode"；
- toString(null)→NPE；含 null 元素集合→"---------"（null 视为缺位）；
- asFileAttribute：name/value 契约、防御拷贝快照（源集合后续修改不影响）、null→NPE、空集合法；
- Files 集成：createFile 精确施加 rw-r-----（umask 不参与）、mode 000 文件可建可 stat 但 readAllBytes→AccessDeniedException；
- 字符串属性族：getAttribute posix:permissions 为 Set、basic:size 为 Long、readAttributes "posix:permissions,size" 键集、
  posix:nosuch→IAE、bogusview:x→UOE。
本机实测断言数：72 条（JDK-27 侧 87 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
