# java.io.File 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐路径分量（getName/getParent/getAbsoluteFile/isAbsolute）、
> 文件系统查询（exists/isFile/isDirectory/length）、生命周期（createNewFile/mkdir/mkdirs/
> renameTo/delete）、list/listFiles、equals/hashCode/compareTo、createTempFile、toPath/toFile 往返。
> 所有临时文件放入隔离目录并在 finally 中清理。

## 1. API 提供了哪些功能

- **构造器**：`(String)`、`(String,String)`、`(File,String)`、`(File,File)`、`(URI)` 等。
- **路径分量**：`getName`、`getParent`、`getParentFile`、`getPath`、`getAbsolutePath`/`getAbsoluteFile`、`getCanonicalPath`/`getCanonicalFile`、`isAbsolute`。
- **文件系统查询**：`exists`、`isFile`、`isDirectory`、`length`、`list`/`listFiles`、`canRead`/`canWrite` 等。
- **生命周期**：`createNewFile`、`mkdir`/`mkdirs`、`renameTo`、`delete`/`deleteOnExit`。
- **工具/比较**：`createTempFile`（2/3 参）、`equals`/`hashCode`/`compareTo`、`toPath`/`toURI`。

## 2. Java 用户如何使用

- `new File(dir, name)` 构造路径（平台无关，不拼分隔符）；`createTempFile` 生成唯一临时文件。
- 依赖 `exists/isFile/isDirectory/length` 查询；`list` 返回子项名（不存在时为 null）；`mkdirs` 递归建目录。
- 依赖 `renameTo`/`delete` 管理文件；`equals/hashCode/compareTo` 按路径比较。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/File.html`）：各方法契约、非空目录 delete 返回 false、list 不存在时返回 null、equals 按路径比较。
- **JDK 实现源码**（`src/share/classes/java/io/File.java`）：`getParentFile` 返回 null 当无父路径、`compareTo` 依字典序比较路径。

## 4. 测试用例设计（映射到 FileTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 路径分量：getName/getParentFile/getAbsoluteFile/isAbsolute/嵌套路径用 File.separator | testPathComponents |
| P0 | 生命周期：mkdirs、createNewFile、exists/isFile/isDirectory、length、renameTo | testLifecycle |
| P1 | list/listFiles 排序一致 | testList |
| P1 | equals/hashCode/compareTo 按路径一致 | testEquality |
| P0 | 非空目录 delete false、逐级 delete、不存在查询全 false、list null、length 0 | testDeleteAndMissing |
| P1 | createTempFile 命名/父目录、toPath/toFile 往返 | testTempAndRoundtrip |

## 关键规范细节（易错，已锁入断言）

- **非空目录 `delete()` 返回 false**（不会递归删除内容）。
- **不存在**路径：`exists/isFile/isDirectory` 全 false、`length()==0`、`list()==null`。
- `getParentFile()` 对无父路径返回 null；`getAbsoluteFile()` 返回绝对路径的 File。
- 嵌套路径**不硬编码 `/`**，用 `new File(new File(d,"a"),"b")` 与 `File.separator`。
- 临时文件/目录放进隔离目录，`finally` 中递归清理。

## 注意事项

- 依赖真实文件系统但仅在隔离临时目录内操作，无墙钟/随机/Locale/Charset/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（继承 Object、实现 Serializable/Comparable、separator 常量、toPath 存在）。

## 深化追加（契约级）

> 双侧探针先行（JDK-8 / JDK-27 实测），新增 testCtorNullsAndMatrix 等 15 个方法，
> 断言 36 → 235。以下行为双侧一致，除特别标注外消息为双侧相同子串。

### 新增维度清单
1. **构造矩阵**：4 个构造器 + null 分支：`File((String)null)` NPE、`File((String)null,"c")` 合法（等价 `File("c")`）、`File((File)null,"c")` 合法、child=null NPE、`File((URI)null)` NPE。
2. **归一化**：getPath 保留 `..`/`.`（"a/../b"、"a/./b" 原样），但**折叠重复分隔符**（"a//b"→"a/b"）并**去尾分隔符**（"a/b/"→"a/b"）；getCanonicalPath 消解 `..`/`.`/重复分隔符（"a/../b"≡"b"、"./x"≡"x"、"a/b/.."≡"a"）；getAbsolutePath 仅前缀 cwd 不消解。
3. **特殊路径**：`File("")` 合法（name=""，parent=null）；单名 parent null；"a/b/" 的 name/parent 均按去尾后计算。
4. **大小写（Linux）**：`File("A")!=File("a")`、hashCode 不同、compareTo 按字典序（'A'<'a'）；compareTo(null) NPE；equals(非 File) false。POSIX 特有断言以 `separatorChar=='/'` 门控。
5. **权限布尔矩阵**：新文件 canRead/canWrite true、canExecute false；setReadOnly→canWrite false（仍 canRead）；setWritable(true/false) 往返；setReadable(false)→false、(true,true)→true、**(false,false) 清空所有读位（owner 也失读）**、(true,false) 全置；setExecutable 同型矩阵。
6. **lastModified**：新文件 >0；setLastModified 精确往返（Linux ms）；(-1)→IAE "Negative time"；0 合法且读回 0；缺失文件 0、setLastModified false。
7. **createNewFile/delete**：已存在 false；父缺失→IOException "No such file or directory"；delete 缺失 false、非空目录 false。
8. **mkdir/mkdirs**：已存在均 false；mkdir 父缺失 false；mkdirs 穿过文件分量 false；多层级联 true + 中间层存在。
9. **list/listFiles**：普通文件/缺失 → null（含带过滤重载）；空目录 len 0；null 过滤器等价全列（三种重载）。
10. **renameTo**：同目录 true；缺失源 false；目标父缺失 false；renameTo(null) NPE；POSIX：目标已存在被替换（true）、自改名 true（均门控 `separatorChar=='/'`）。
11. **URI**：toURI scheme=file、绝对、round-trip equals；toURL "file:" 前缀；URI 构造 6 个负例（消息双侧实测一致）。
12. **createTempFile**：前缀 <3 → IAE（消息见下）、前缀 null → NPE、后缀 null → ".tmp"、目录 null → java.io.tmpdir、目录缺失 → IOException；deleteOnExit 幂等且对缺失文件不抛。
13. **listRoots/常量/toString**：Linux roots=["/"]、"/" exists/isDirectory/isAbsolute、name=""；separator/pathSeparator 字串与字符一致；toString==getPath。
14. **空间/isHidden**：真实目录三空间 >0；缺失全 0；Linux isHidden 按名 "." 前缀（缺失文件同样生效）。
15. **序列化**：round-trip equals/compareTo==0/path 保留；serialVersionUID=301077366599181567 双侧一致。

### 负例精确异常表（双侧实测）
| 操作 | 异常 | 消息（子串） |
|---|---|---|
| new File((String)null) / child null / (URI)null | NullPointerException | —（类型级） |
| new File(uri).compareTo(null) | NullPointerException | — |
| renameTo(null) | NullPointerException | — |
| File.createTempFile(null,…) | NullPointerException | — |
| File.createTempFile("ab",…) | IllegalArgumentException | "too short" |
| setLastModified(-1) | IllegalArgumentException | "Negative time" |
| createNewFile（父缺失） | IOException | "No such file or directory" |
| createTempFile（目录缺失） | IOException | "No such file or directory" |
| new File(URI.create("a/b")) | IllegalArgumentException | "URI is not absolute" |
| new File(URI.create("http://x/y")) | IllegalArgumentException | "URI scheme is not \"file\"" |
| new File(URI.create("mailto:a@b")) | IllegalArgumentException | "URI is not hierarchical" |
| new File(URI.create("file:///a/b#f")) | IllegalArgumentException | "URI has a fragment component" |
| new File(URI.create("file:///a/b?q")) | IllegalArgumentException | "URI has a query component" |
| new File(URI.create("file://host/a/b")) | IllegalArgumentException | "URI has an authority component" |

### JDK-8 vs JDK-27 版本差异
- createTempFile 前缀过短消息：8=`Prefix string too short`，27=`Prefix string "ab" too short: length must be at least 3`（断言公共子串 "too short"）。
- NPE 消息：8 均为 null，27 带 helpful 提示（断言只到类型级）。
- 其余行为（归一化/权限/URI/序列化 svUID）双侧一致。
