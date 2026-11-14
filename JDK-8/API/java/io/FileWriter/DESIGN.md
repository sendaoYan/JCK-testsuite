# java.io.FileWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(File)`、`(File, boolean append)`、`(String path)`、`(String, boolean append)`、`(FileDescriptor)`。
- 写：`write(String)`、`write(String,int,int)`、`write(char[],int,int)`、`append(char)`、`append(CharSequence)`。
- 语义：默认 append=false 截断；append=true 追加；`flush()`/`close()` 落盘。
- 注意：JDK 8 无 `FileWriter(File, Charset)`（JDK 11+）。

## 2. Java 用户如何使用
- `try (FileWriter fw = new FileWriter(f)) { fw.write(text); }` 写文本文件；`(f, true)` 追加日志。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new FileWriter(file)` — `jdk8u-dev/jdk/src/share/classes/java/io/FileWriter.java`（薄封装 OutputStreamWriter）。
- 追加写 — 大量工具代码 `new FileWriter(f, true)`。

## 4. 测试用例设计 + 自动化实现（FileWriterTest.java，纯黑盒）
黑盒场景（ASCII 内容保证默认编码确定性）:
1. write("ab") → 长度 2；append(char) 追加 → "abcd" 读回。
2. 默认模式截断 → 长度 2 + "xy" 读回。
3. write(String,off,len) 区域写 → "bcd"。
4. String 构造 + append + flush → "bcdz"。
5. write(char[],off,len) 区域写 → "QR"。
- 健壮性: ASCII 内容跨默认编码一致；临时目录 + 递归清理。

## 深化追加（契约级）

新增维度（断言 9 → 55）：
- 构造负例：null File/String（含 boolean 变体）→ NPE；缺失父目录 → FNFE 含平台 OS 原因文本与路径；目录 → FNFE 含平台 OS 原因文本（含 append=true）。
- FileDescriptor 构造：经 FileOutputStream 的 FD 写 "ok" 读回。
- 关闭矩阵：write(int)/write(char[])/write(String)/flush → IOException "Stream closed"；close 幂等；关闭前未写文件长 0。
- truncate vs append 全构造组合：File/String × false/true；append 空串 no-op。
- write 越界：(String)null NPE、(char[])null NPE、off+len 溢出 IOOBE、write("abc",1,5) StringIndexOOB；失败后流仍可写。
- write(int) 取低 16 位（0x10041→'A'）+ append('C') → "ABC"。
- flush 可见性：flush 后 f.length()==7；未 flush 的缓冲字符不可见；close 补齐至 8。
- 逐字符 10 连写；500 字符大写往返（readAll 循环读）。
- append 语义：返回 this；append(null)→"null"；"cdef"[1,3)=="de"（区间端点笔误坑）；append("") no-op → "ABnullde"。
- 4 种写形式（write(int)/write(char[])/write(String)/append(char)）产出相同文件。
- 继承 getEncoding：打开非 null / 关闭 null（OutputStreamWriter 契约）。
- 重开语义：默认截断 / append 扩展；空写 + 双 flush no-op。
- 内容全 ASCII（JDK-8 无 Charset 构造）。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| null File/String（4 形式） | NullPointerException | 类型级 |
| 缺失父目录 | FileNotFoundException | 平台 OS 原因文本 |
| 打开目录（含 append=true） | FileNotFoundException | 平台 OS 原因文本 |
| 关闭后 write/flush | IOException | Stream closed |
| write("abc",1,5) | StringIndexOutOfBoundsException | 类型级 |
| write(cbuf,off,len) 越界 | IndexOutOfBoundsException | 类型级 |

版本差异：JDK-8 无 Charset 构造（JDK-11+），charset 维度在 JDK-27 侧覆盖。
