# java.nio.file.attribute.UserPrincipalNotFoundException 设计文档

## 1. API 功能

IOException 子类：携带查找失败的主体名 getName()（message 为 null）；Serializable，
serialVersionUID 固定 -5369283889045833024L。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

catch (UserPrincipalNotFoundException e) 精确区分"主体不存在"与其它 IO 失败；可按 getName 重试/提示。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/UserPrincipalLookupService/Basic.java：异常触发与名字断言；
- UserPrincipalNotFoundException 源码：仅 name 字段、无 message。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`UserPrincipalNotFoundExceptionTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：构造名字往返、ctor(null)→getName null、getMessage null、getCause null、
instanceof/extends IOException、toString 精确串（无 detail 后缀）、
同名孪生不等（异常恒等语义）、initCause/getCause、
serialVersionUID 固定值（ObjectStreamClass）+ 序列化往返名字保真、
由默认查找服务对未知用户抛出（JDK-8 getName 保留所查名；JDK-25+ 实测返回空串——双侧各按实测断言）、可按 IOException 捕获。
本机实测断言数：19 条（JDK-27 侧 30 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
