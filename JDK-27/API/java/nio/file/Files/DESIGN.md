# java.nio.file.Files 测试方案

## 1. API 功能
java.nio.file.Files 是文件系统操作的静态工具类 (final class, extends Object, 全 static)。
JDK-27 在 JDK-8 基础上新增 (以 javap -public 实测为准)：
- writeString(Path,CharSequence[,Charset],OpenOption...)/readString(Path[,Charset]) (JDK-11+)
- mismatch(Path,Path) (JDK-12+)
其余 JDK-8 已有: 创建/删除、copy/move、readAllBytes/readAllLines/write(byte[]|Iterable)、
newBufferedReader/Writer、list/walk/find/lines、exists/notExists/isDirectory/isRegularFile/
isSymbolicLink/isSameFile/size/getLastModifiedTime/getFileStore/probeContentType、属性视图等。

## 2. Java 用户如何使用
- 常规用法: createTempDirectory/createTempFile 建临时资源 -> writeString/readString (UTF-8) ->
  readAllLines -> copy/move (REPLACE_EXISTING) -> size/mismatch -> newBufferedWriter/Reader ->
  deleteIfExists 清理。
- 组合用法: 与 Path.of、StandardCharsets、Stream (list/walk/lines)、BufferedReader/Writer 组合。
- 生命周期: 临时资源 finally 递归删除；AutoCloseable 的 Stream/Reader/Writer 用 try-with-resources。
- 失败路径: IOException/NoSuchFileException/FileAlreadyExistsException 等精确报告失败。

## 3. 开源代码实际用法调研
- 通用形态: 优先走 Files 高层门面 (读写/遍历/复制)，仅需精细控制时才下探通道。
- 测试侧惯例: 临时目录 + finally 递归删除保证无残留；相对路径拼接用 Path.resolve 而非手拼分隔符。

## 4. 测试用例设计 + 自动化实现
文件: `FilesTest.java` (单文件，testWhiteBox() + testBlackBox()，由 main 依次调用)。

白盒 (反射): final class/extends Object；hasMethod 校验 writeString/readString/readAllLines/copy/
move/createTempDirectory 等关键签名。

黑盒 (行为): 临时目录生命周期 —— createTempDirectory -> createDirectory/createTempFile ->
writeString/readString (含非 ASCII 往返) -> size == UTF-8 字节数 -> write(Iterable)/readAllLines ->
write/readAllBytes -> copy + REPLACE_EXISTING -> move + REPLACE_EXISTING -> mismatch (相等与差异下标)
-> newBufferedWriter/Reader -> Files.lines -> Files.list -> deleteIfExists(true/false) -> delete。

健壮性: 全部在 Files.createTempDirectory("jck-files") 下进行，finally 递归删除；无墙钟/随机/
全局状态/网络依赖；非 ASCII 用 UTF-8 字面量；不硬编码路径分隔符 (Path.of(dir.toString(),...) 与
dir.resolve(...))；headless 安全不加 @key。
本机实测断言数: 30 条。

## 深化追加（契约级，批次 A9）

新增维度（断言 25 → 239）：与 JDK-8 侧同套（存在性 NOFOLLOW、createTemp 矩阵、字节/行/流读写、
copy 目录语义、move 矩阵含 ATOMIC_MOVE、delete 非空 -> DirectoryNotEmptyException、
readSymbolicLink -> NotLinkException、newInputStream(WRITE/APPEND) -> UOE、newOutputStream(READ)
-> IAE、walk 负深度 IAE、maxDepth==0、walkFileTree 非存在起点 visitFileFailed、basic:* 恰 9 键、
probeContentType）；另加 testStringMethods（JDK-11+，版本注释）：writeString 默认 UTF-8/显式
UTF-16、readString 往返、mismatch 相等 -1/首差 2/前缀短长度/空 0。

版本差异（重大，实测）：isSameFile 两不存在路径 JDK-8 抛 NoSuchFileException、JDK-27 返回 false
（本版断言不抛）；readString/writeString/mismatch 仅本版。
