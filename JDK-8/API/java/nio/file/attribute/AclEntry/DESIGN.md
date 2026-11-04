# java.nio.file.attribute.AclEntry 设计文档

## 1. API 功能

ACL 条目不可变值类型：type/principal/permissions/flags 四字段 + newBuilder/newBuilder(entry) 工厂；
permissions()/flags() 每次返回新拷贝；equals/hashCode 按四字段值语义；toString 格式
principal[:perms][/…][:flags][/…]:type（空段省略）；无公共构造器、无 clone、非 Serializable。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

ACL 文件系统（NFSv4/ZFS/Windows）上经 AclFileAttributeView.getAcl/setAcl 读写条目列表；
条目值对象可自由缓存/比较。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/AclEntry/Basic.java：builder/equals/toString 断言；
- sun.nio.fs.UnixAclFileAttributeView（非 Linux 主路径）与 AclEntry 源码中防御拷贝与
  "Missing type/who component" 校验。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`AclEntryTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：getter 四字段、permissions()/flags() 逐次新拷贝（外部 add 不污染）、
源集合快照隔离、equals/hashCode 四分量变体矩阵 + null/异型、varargs 与 Set 重载等价、
默认空 permissions/flags、toString 5 种精确格式（含全 14 权限/4 标志宽条目）、
newBuilder(entry) 拷贝独立、结构白盒（final/无私有构造器暴露/无 clone/非 Serializable）。
本机实测断言数：41 条（JDK-27 侧 64 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
