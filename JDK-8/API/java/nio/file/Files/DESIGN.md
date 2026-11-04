# java.nio.file.Files 测试方案

## 1. API 功能
java.nio.file.Files 是文件系统操作的静态工具类 (final class, extends Object, 全 static)。
JDK-8 核心方法 (以 jdk8u372 javap -public 实测为准)：
- 创建/删除: createFile/createDirectory/createDirectories/createTempFile/createTempDirectory/
  createSymbolicLink/createLink/delete/deleteIfExists
- 复制/移动: copy(Path,Path,CopyOption...)/move(Path,Path,CopyOption...)/copy(InputStream,Path,...)/
  copy(Path,OutputStream)
- 读写: newInputStream/newOutputStream/newByteChannel/newBufferedReader/newBufferedWriter/
  readAllBytes/readAllLines(Path[,Charset])/write(Path,byte[],...)/write(Path,Iterable[,Charset],...)
- 遍历: list/walk/find/lines/newDirectoryStream/walkFileTree
- 查询: exists/notExists/isDirectory/isRegularFile/isSymbolicLink/isHidden/isReadable/isWritable/
  isExecutable/isSameFile/size/getLastModifiedTime/getFileStore/probeContentType
- 属性: readAttributes/getAttribute/setAttribute/getFileAttributeView/posix/user 视图
- 注: JDK-8 无 writeString/readString (JDK-11+)、无 mismatch (JDK-12+)。

## 2. Java 用户如何使用
- 常规用法: Files.createTempDirectory("prefix") 建临时目录 -> 在其下 createTempFile/写字节/写行 ->
  readAllBytes/readAllLines -> copy/move -> size/属性查询 -> deleteIfExists/delete 清理。
- 组合用法: 与 Path (resolve 拼接)、StandardCharsets (UTF-8 编解码)、BufferedReader/Writer、
  Stream (list/walk/lines) 组合。
- 生命周期: 临时资源 finally 递归删除；AutoCloseable 的 Stream/Reader/Writer 用 try-with-resources。
- 失败路径: IOException/NoSuchFileException/FileAlreadyExistsException 等精确报告失败。

## 3. 开源代码实际用法调研
- 通用形态: 优先走 Files 高层门面 (读写/遍历/复制)，仅需精细控制时才下探通道。
- 测试侧惯例: 临时目录 + finally 递归删除保证无残留；相对路径拼接用 Path.resolve 而非手拼分隔符。

## 4. 测试用例设计 + 自动化实现
文件: `FilesTest.java` (单文件，testWhiteBox() + testBlackBox()，由 main 依次调用)。

白盒 (反射): final class/extends Object；hasMethod 校验 24 个关键方法签名
(readAllBytes/readAllLines/write(byte[])/write(Iterable,Charset)/copy/move/createDirectory(s)/
createTempDirectory/createTempFile/newBufferedReader/newBufferedWriter/list/walk/lines/isSameFile/
isSymbolicLink/getFileStore/probeContentType/exists/notExists/size/deleteIfExists)。

黑盒 (行为): 临时目录生命周期 —— createTempDirectory -> createDirectory/createDirectories ->
createTempFile -> write(byte[])/readAllBytes + write(Iterable,Charset)/readAllLines (含非 ASCII 往返)
-> size == UTF-8 字节数 -> copy + REPLACE_EXISTING -> move + REPLACE_EXISTING -> newBufferedWriter/
Reader 往返 -> Files.lines -> Files.list 列出子项 -> isSameFile/isSymbolicLink/getFileStore/
getLastModifiedTime/isReadable/isWritable/probeContentType -> deleteIfExists(true/false) -> delete 目录。

健壮性: 全部在 Files.createTempDirectory("jck-files") 下进行，finally 递归删除；无墙钟/随机/
全局状态/网络依赖；非 ASCII 用 UTF-8 字面量 (jtreg 以 UTF-8 编译)；不硬编码路径分隔符
(全部 dir.resolve(...))；headless 安全不加 @key。
本机实测断言数: 56 条。
