# java.nio.file.attribute.BasicFileAttributeView 设计文档

## 1. API 功能

基础属性视图：readAttributes()、setTimes(mtime, atime, ctime)（null 参数 = 不修改）。name() 固定 "basic"。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

getFileAttributeView(path, BasicFileAttributeView.class) 后设置时间戳；posix/dos 视图均继承本接口；
Files.setLastModifiedTime/setAttribute("basic:*") 走同一通道。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/BasicFileAttributeView/Basic.java：setTimes null 组合与精度；
- sun.nio.fs.UnixFileAttributeViews$Basic：futimens 封装、ctime 不可设说明。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`BasicFileAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：name/basic、mtime 毫秒精确往返、纪元前往返、atime-only 不动 mtime、
ctime-only 在 Linux 无操作（mtime/atime 不变）、全 null 显式无操作、
posix/dos 视图 instanceof BasicFileAttributeView 继承矩阵、缺失文件 setTimes/readAttributes 负例、
Files.set/getLastModifiedTime 往返、setAttribute/getAttribute("basic:lastModifiedTime") 往返。
本机实测断言数：25 条（JDK-27 侧 36 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
