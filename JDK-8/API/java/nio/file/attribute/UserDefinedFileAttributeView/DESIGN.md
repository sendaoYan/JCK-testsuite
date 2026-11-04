# java.nio.file.attribute.UserDefinedFileAttributeView 设计文档

## 1. API 功能

用户自定义（扩展）属性视图：list()/size(name)/read(name,ByteBuffer)/write(name,ByteBuffer)/delete(name)。
name() 固定 "user"；值是原始字节。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

文件附加元数据（mime 类型、校验和等）：uv.write("mimetype", wrap(bytes)) 写、read 读回、list 列名、
delete 删除；Files.setAttribute("user:x", byte[]) 同通道。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/UserDefinedFileAttributeView/Basic.java：list/read/write/delete 往返与
  小缓冲区异常；
- sun.nio.fs.LinuxUserDefinedFileAttributeView：getxattr/setxattr/listxattr/removexattr 封装
  （ERANGE→"Insufficient space in buffer"、ENODATA→missing attribute）。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`UserDefinedFileAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：write 返回字节数与 size 精确、read 返回数/缓冲 position 前进/内容逐字节、
覆盖更新 size 与内容、多属性并存 list 包含、delete 后 list 移除且他属性不动、
零长度属性（size 0、read 返 0）、write 只传 remaining（position 偏移）、read 从 position 起填充、
负例精确类型：缺属性 read/size/delete→FileSystemException（ENODATA）、缓冲过小/零容量→FileSystemException（ERANGE）、
null（名字/缓冲）→NPE、空名→FileSystemException、Files.getAttribute("user:缺失")→FileSystemException、
Files.setAttribute/getAttribute byte[] 往返、目录同样可用。
本机实测断言数：42 条（JDK-27 侧 56 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
