# java.lang.StringIndexOutOfBoundsException 测试方案

## 1. API 功能（按方法分组）

- 构造器：`()`（message null）、`(String s)`（逐字消息）、`(int index)`（消息 `String index out of range: <index>`，本 JDK-8u 已含，javap 确认）。
- 继承自 Throwable 的通用契约：`getMessage`/`getLocalizedMessage`、`getCause`/`initCause`（一次性）、`addSuppressed`/`getSuppressed`（保序 + 防御性拷贝）、`getStackTrace`/`printStackTrace`、`toString`（`ClassName: message`）、`fillInStackTrace`（构造即捕栈）。
- 层级：`StringIndexOutOfBoundsException` → `IndexOutOfBoundsException` → `RuntimeException` → `Exception` → `Throwable`；`Serializable`；与 `ArrayIndexOutOfBoundsException` 是兄弟。
- 语义：由字符串/字符序列索引越界触发（`String.charAt/substring`、`StringBuilder/StringBuffer.charAt/substring/...`）。

## 2. Java 用户如何使用

- 捕获并按索引诊断：`try { s.charAt(i); } catch (StringIndexOutOfBoundsException e) { ... e.getMessage() ... }`。
- 按父类捕获：`catch (IndexOutOfBoundsException e)` 可同时覆盖数组与字符串越界。
- 主动构造带索引的实例：`throw new StringIndexOutOfBoundsException(index);`（消息自动格式化）。

## 3. 参考来源

- Oracle javadoc StringIndexOutOfBoundsException / Throwable（addSuppressed/getSuppressed 防御性拷贝、initCause 一次性语义）。
- JDK-8（jdk8u372-b07）与 JDK-27 双侧 javap 枚举构造器 + 实测消息文本（临时探针程序放 /tmp 分别用两侧 javac/java 运行）。
- OpenJDK test/jdk 中异常触发模式（仅借鉴思路）。

## 4. 测试用例设计 + 自动化实现

- 白盒：`Class.forName` 断言非接口、父类、可赋值性、公开构造器数量恰 3。
- 黑盒：三个构造器逐个断言 getMessage/getCause/toString；`(int)` 直调与反射两条路径；initCause 往返 + 二次 IllegalStateException；addSuppressed 保序 + 防御性拷贝 + null NPE 消息；getStackTrace 顶帧；printStackTrace(PrintStream) 内容（类名行/帧行/Suppressed 段）；真实越界触发（`"abc".charAt(5)` 等）断异常类精确 + 消息逐字（MODERN 分支）；父类捕获。
- 自动化：`StringIndexOutOfBoundsExceptionTest`（单一 main 类，jtreg `@run main`），`check(cond,msg)` 计数到 `PASS (N assertions)`。

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）
- 层级：直接父类 `IndexOutOfBoundsException`、可赋值性（RuntimeException/Exception/Serializable）、与 `ArrayIndexOutOfBoundsException` 是兄弟（`Class.isAssignableFrom` 断言非子类型）、公开构造器恰 3 个。
- 构造器矩阵：`()`（null 消息）、`("m")`（逐字 + toString `ClassName: message` + cause null）、`(int)` **直调**（本 JDK-8u 已含该构造器，javap 确认；消息 `String index out of range: n` 对 5/0/-3/Integer.MIN_VALUE 逐字锁定且与 JDK-27 逐字相同）。
- Throwable 通用契约：initCause 返回 this + 二次抛 `IllegalStateException`、`(String,Throwable)` 包装模式接 cause（注意：SIOOBE 本身无 `(String,Throwable)` 构造器，javap 双侧确认仅 3 个公开构造器，cause 只能经 initCause 接线）。
- 抑制异常：addSuppressed 保序保身份、getSuppressed 防御性拷贝（改返回数组不影响二次读取）、`addSuppressed(null)` → NPE 且消息 `Cannot suppress a null exception.` **两侧逐字相同**。
- 栈与打印：构造即捕栈（顶帧为测试类）、printStackTrace(PrintStream) 含 `类名: 消息` 行、帧行与 `Suppressed:` 段。
- 真实触发：`String.charAt(5)`、`String.substring(5)`、`StringBuilder.charAt(-1)`、`StringBuffer.substring(4)`、`CharSequence.charAt(7)` —— 异常类精确为 SIOOBE，消息经 MODERN 分支逐字锁定（8 `String index out of range: ...` / 27 `Index n out of bounds for length L`、`Range [...]`）；可被父类 `IndexOutOfBoundsException` 捕获。

### 使用方式
- 同前：`check(cond,msg)` 计数；`MODERN` 常量（`!java.version.startsWith("1.")`）编码两侧消息差异，同一份源文件两侧可编译运行。

### 参考来源
- Oracle javadoc StringIndexOutOfBoundsException / Throwable（addSuppressed/防御性拷贝语义）；JDK-8 与 JDK-27 双侧实测（含 javap 构造器枚举）。

### 测试用例设计 + 易错规范细节（本轮锁定）
- **`(int)` 构造器两侧都存在**（javap 双侧确认 3 个公开构造器），消息格式 `String index out of range: <index>` 在 JDK-8 与 JDK-27 完全一致 —— 无分支直接锁定。
- **真实越界的消息文本随版本变化**：`"abc".charAt(5)` JDK-8 `String index out of range: 5` vs JDK-27 `Index 5 out of bounds for length 3`；`"abc".substring(5)` JDK-8 消息为 `String index out of range: -2`（begin-length 差值）vs JDK-27 `Range [5, 3) out of bounds for length 3` —— MODERN 分支各自锁定。
- **`addSuppressed(null)` 的 NPE 消息 `Cannot suppress a null exception.` 两侧逐字相同**。
- **SIOOBE 无 (String,Throwable) 构造器**：cause 链只能经 initCause；本测试用 `new Exception("w", sioobe)` 演示包装方向。
