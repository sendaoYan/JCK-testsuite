# java.io.File 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造: `File(String)`、`File(String,String)`、`File(File,String)`、`File(URI)`
- 名称/路径分量: `getName`、`getPath`、`getParent`、`getParentFile`、`isAbsolute`、`getAbsolutePath`、`getAbsoluteFile`、`getCanonicalPath`、`getCanonicalFile`
- 状态查询: `exists`、`isFile`、`isDirectory`、`isHidden`、`canRead`、`canWrite`、`canExecute`、`lastModified`、`length`
- 文件系统操作: `createNewFile`、`delete`、`deleteOnExit`、`mkdir`、`mkdirs`、`renameTo`、`setLastModified`、`setReadOnly`、`setWritable/setReadable/setExecutable`
- 列目录: `list()`、`list(FilenameFilter)`、`listFiles()`、`listFiles(FilenameFilter)`、`listFiles(FileFilter)`
- 空间: `getTotalSpace`、`getFreeSpace`、`getUsableSpace`
- 静态工具: `createTempFile(String,String[,File])`、`listRoots`
- 转换/契约: `toURL`、`toURI`、`toPath`、`toString`、`compareTo`、`equals`、`hashCode`

## 2. Java 用户如何使用
- `File.createTempFile("jck", ".tmp")` 建临时文件；`new File(dir, "a/b")` 拼接子路径。
- 典型组合链路：`mkdirs` 建目录 → 放文件 → 写内容 → `length`/`list` 查询 → `renameTo` 改名 → `delete` 清理。
- `exists/isFile/isDirectory` 组合判断文件系统状态；`toPath().toFile()` 与 `java.nio.file` 互转。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `File tmpFile = File.createTempFile("MetaInf", null, null);` — `jdk25u-dev/test/jdk/tools/jar/index/MetaInf.java:123`
- `File tmp = File.createTempFile("system-out-err", ".txt");` — `jdk25u-dev/test/jdk/tools/jar/UpdateManifest.java:53`
- `File manifestOrig = File.createTempFile("manifestOrig", ".txt");` — `jdk25u-dev/test/jdk/tools/jar/UpdateManifest.java:91`
- `FileSystems.getDefault().getPath(this.getPath());`（File.toPath 内部实现）— `jdk25u-dev/src/java.base/share/classes/java/io/File.java:2076`

## 4. 测试用例设计 + 自动化实现（指向 FileTest.java 黑盒 `testBlackBox`）
- 正常/组合: 独立临时目录内 mkdirs → createNewFile → 写 5 字节 → length=5 → list/listFiles 排序断言 → renameTo → toPath/toFile 往返 → equals/hashCode/compareTo → 逐级 delete。
- 边界: 不存在文件 `exists==false/isFile==false/length==0`；非空目录 `delete()==false`；`createTempFile` 前缀/后缀命名。
- 健壮性: 全程在 `Files.createTempDirectory("jck-file")` 独立目录内操作，`finally` 递归清理；排序后再断言 list 结果；不依赖默认编码/墙钟/全局状态。
- 白盒 `testWhiteBox`: 反射校验类形态(非接口/非抽象、继承 Object、实现 Serializable+Comparable、关键字段与方法存在)。
