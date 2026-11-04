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

## 深化追加（契约级）

> 双侧探针先行（JDK-8 / JDK-27 实测），新增 testCtorNullsAndMatrix 等 15 个方法，
> 断言 29 → 229。以下行为双侧一致，除特别标注外消息为双侧相同子串。

### 新增维度清单
1. **构造矩阵**：4 个构造器 + null 分支：`File((String)null)` NPE、`File((String)null,"c")` 合法（等价 `File("c")`）、`File((File)null,"c")` 合法、child=null NPE、`File((URI)null)` NPE。
2. **归一化**：getPath 保留 `..`/`.`（"a/../b"、"a/./b" 原样），但**折叠重复分隔符**（"a//b"→"a/b"）并**去尾分隔符**（"a/b/"→"a/b"）；getCanonicalPath 消解 `..`/`.`/重复分隔符；getAbsolutePath 仅前缀 cwd 不消解。
3. **特殊路径**：`File("")` 合法（name=""，parent=null）；单名 parent null；"a/b/" 的 name/parent 均按去尾后计算。
4. **大小写（Linux）**：`File("A")!=File("a")`、hashCode 不同、compareTo 按字典序；compareTo(null) NPE；equals(非 File) false。POSIX 特有断言以 `separatorChar=='/'` 门控。
5. **权限布尔矩阵**：新文件 canRead/canWrite true、canExecute false；setReadOnly→canWrite false；setWritable(true/false) 往返；setReadable/ setExecutable 含 ownerOnly 二参数全矩阵（(false,false) 清空所有位）。
6. **lastModified**：新文件 >0；setLastModified 精确往返；(-1)→IAE "Negative time"；0 合法；缺失文件 0。
7. **createNewFile/delete**：已存在 false；父缺失→IOException "No such file or directory"；非空目录 delete false。
8. **mkdir/mkdirs**：已存在 false；父缺失 false；穿过文件分量 false；多层级联 true。
9. **list/listFiles**：普通文件/缺失 → null；空目录 len 0；null 过滤器等价全列。
10. **renameTo**：同目录 true；缺失源 false；目标父缺失 false；renameTo(null) NPE；POSIX 覆盖目标/自改名 true。
11. **URI**：toURI/toURL round-trip；URI 构造 6 个负例（消息双侧一致）。
12. **createTempFile**：前缀<3 IAE、null 前缀 NPE、null 后缀 ".tmp"、null 目录 tmpdir、目录缺失 IOE；deleteOnExit 幂等。
13. **listRoots/常量/toString**：Linux roots=["/"]；分隔符常量一致；toString==getPath。
14. **空间/isHidden**：真实目录 >0、缺失 0；Linux 按名 "." 前缀判 hidden。
15. **序列化**：round-trip equals/compareTo==0/path；svUID=301077366599181567。

### 负例精确异常表（双侧实测）
（同 JDK-8 侧 DESIGN 的表：URI 六负例消息、createTempFile "too short"、"Negative time"、"No such file or directory"×2、NPE×4。）

### JDK-8 vs JDK-27 版本差异
- createTempFile 前缀过短消息：27=`Prefix string "ab" too short: length must be at least 3`（8 为 `Prefix string too short`；断言公共子串 "too short"）。
- NPE 消息：27 带 helpful 提示（如 `Cannot invoke "String.length()" because "prefix" is null`），8 均为 null。
- 其余行为双侧一致（含 File 序列化 svUID）。
