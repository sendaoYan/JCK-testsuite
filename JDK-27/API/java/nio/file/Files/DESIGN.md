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
