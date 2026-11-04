# java.nio.file.SecureDirectoryStream 测试方案

## 1. API 功能
java.nio.file.SecureDirectoryStream<T> extends DirectoryStream<T>: 以打开的目录 fd 为基准的
相对操作 (相对路径不受并发 rename/替换影响)。
JDK-8 方法 (以 jdk8u372 javap 实测为准)：
- newDirectoryStream(T path[, Filter)/LinkOption...]) — 相对打开子目录流
- newByteChannel(T path, Set<? extends OpenOption>, FileAttribute...) — 相对打开通道
- deleteFile(T)/deleteDirectory(T) — 相对删除
- move(T srcpath, SecureDirectoryStream<? extends T> targetdir, T targetpath) — 跨流移动
- getFileAttributeView(Class<V>)/getFileAttributeView(T path, Class<V>, LinkOption...) — 视图

## 2. Java 用户如何使用
- 常规用法: (SecureDirectoryStream) Files.newDirectoryStream(dir) 强转获得 (Linux sun.nio.fs 支持，
  不支持时 instanceof 检查优雅跳过)；对相对名做 open/delete/move。
- 组合用法: 双流 move 实现安全 rename；属性视图直接绑定 fd 路径。

## 3. 开源代码实际用法调研
- 通用形态: 安全删除器/同步器以 SDS 避免 TOCTOU；多数代码先 instanceof 探测再降级。

## 4. 测试用例设计 + 自动化实现
文件: `SecureDirectoryStreamTest.java` (testWhiteBox + testBlackBox，main 依次调用)。

白盒 (反射): 接口；extends DirectoryStream；单类型参数 T；hasMethod 六个；
  newByteChannel(T, Set, FileAttribute...) 三参签名。

黑盒 (batch A9 全量重写+探针 /tmp/nio-a9-probe/ProbeSDS 实测):
- 相对 newByteChannel CREATE+WRITE 写入 -> Files 侧可见；相对 READ 读回往返一致。
- 双视图: path 绑定视图与 directory 绑定视图属性正确 (regular file / directory)。
- 嵌套 newDirectoryStream(".") 条目可见。
- 跨流 move + deleteFile；CREATE_NEW+WRITE 对已存在 -> FileAlreadyExistsException (精确)。
- 负例精确类型: deleteDirectory 非空 -> DirectoryNotEmptyException；
  deleteFile/deleteDirectory 缺失 -> NoSuchFileException；
  close 后 newByteChannel/newDirectoryStream/deleteFile -> ClosedDirectoryStreamException。
- 探针修正: CREATE_NEW 单独 (无 WRITE) 打开已存在文件不抛 (实现惰性)；据此断言组合语义。

健壮性: instanceof 不支持时打印 NOTE 跳过；临时目录 + finally 递归删除；headless 不加 @key。
本机实测断言数: 33 条 (旧 4)。
