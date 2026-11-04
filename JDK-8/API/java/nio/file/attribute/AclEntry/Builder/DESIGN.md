# java.nio.file.attribute.AclEntry.Builder 设计文档

## 1. API 功能

AclEntry 的流式构造器：setType/setPrincipal 必填（build 时分别抛 IllegalStateException
"Missing type component"/"Missing who component"），setPermissions/setFlags 可选（Set 与 varargs 重载），
build() 可重复调用、构造器可复用、后设覆盖先设。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

newBuilder().setType(ALLOW).setPrincipal(owner).setPermissions(...).setFlags(...).build() 链式组装；
从已有条目 newBuilder(entry) 派生修改。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/AclEntry/Basic.java：必填校验与 null NPE 等价类；
- AclEntry 源码 Builder：Objects.requireNonNull 逐参数、EnumSet 拷贝快照时机。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`AclEntry.BuilderTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：build 前置条件两 ISE 及精确消息、null 矩阵 10 项 NPE（type/principal、Set/varargs、
含 null 元素的 Set 与 varargs）、链式返回 this（6 setter）、build 两次等值不同对象、复用与覆盖语义、
setPermissions 快照时机、空 varargs→空集、newBuilder(entry) 预填四分量、newBuilder(null) NPE、
Builder final 无公共构造器。
本机实测断言数：37 条（JDK-27 侧 57 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
