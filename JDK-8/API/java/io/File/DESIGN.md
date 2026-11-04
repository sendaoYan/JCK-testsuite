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
