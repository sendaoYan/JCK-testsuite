# java.nio.file.attribute.FileTime 设计文档

## 1. API 功能

不可变文件时间戳值类型：fromMillis(long)/from(long,TimeUnit)/from(Instant) 三工厂，
toMillis()/to(TimeUnit)/toInstant() 三读取，equals/hashCode/compareTo 值语义，toString 输出
ISO-8601 UTC 字符串。精度可达纳秒；不实现 Serializable（writeObject 抛 NotSerializableException）。
实测双侧 API 面一致（javap：均无 from(Duration)）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

Files.setLastModifiedTime/getLastModifiedTime、BasicFileAttributeView.setTimes、
readAttributes().lastModifiedTime() 等均以 FileTime 为货币类型；比较时间戳用 compareTo，
显示用 toString（ISO-8601，UTC）。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/FileTime/Basic.java（JDK-8 同路径）：fromMillis/from(long,unit)
  往返、toString 格式、compareTo 排序等价类；
- sun.nio.fs.UnixFileTimes：纳秒精度存储与毫秒截断的实现位置；
- Files.getAttribute/setAttribute 的 lastModifiedTime 路径。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`FileTimeTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：
- fromMillis 往返矩阵（0/负数/Long.MAX_VALUE/Long.MIN_VALUE）；
- to(TimeUnit) 七种单位换算（截断向零、负数、饱和：MAX 毫秒→微/纳秒饱和为 Long.MAX_VALUE，纳秒往返损失精度 9223372036854）；
- from(long,unit) 矩阵与跨表示 equals（1000ms == 1s，1e9ns == 1s）；
- 亚毫秒精度 compareTo（999ns<1ms<1500ns? 实测 1500ns<1ms、1000000ns==1ms）；
- equals/hashCode（同值相等、null/异型不等、hashCode 一致）；
- compareTo 全序（正负零点对称）；
- toInstant 往返（纳秒保真、toMillis 丢亚毫秒位）；
- toString 14 个精确串（epoch、纪元前、小数秒三种位数、0001/9999 年、远未来 292278994-08-17、远过去 -292275056）；
- 非 Serializable 白盒 + ObjectOutputStream 拒绝；
- Files 集成：mtime 毫秒精确往返 + 纪元前时间戳往返（utimensat 支持）。
本机实测断言数：66 条（JDK-27 侧同设计 94 条，含既有白盒）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
