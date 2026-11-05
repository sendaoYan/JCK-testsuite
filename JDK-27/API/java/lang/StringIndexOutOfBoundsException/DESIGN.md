# StringIndexOutOfBoundsException — 设计方案

## 1. API 功能
`java.lang.StringIndexOutOfBoundsException` 继承自 `IndexOutOfBoundsException`，表示「StringIndexOutOfBoundsException」这一错误/异常条件。
构造器：`StringIndexOutOfBoundsException()`、`StringIndexOutOfBoundsException(String)`、`StringIndexOutOfBoundsException(int)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new StringIndexOutOfBoundsException(...)` 向上层报告：
```java
try {
    "abc".charAt(99);  // 触发 StringIndexOutOfBoundsException
} catch (StringIndexOutOfBoundsException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`string index out of range`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `IndexOutOfBoundsException`、父类为 `IndexOutOfBoundsException`、公开构造器 3 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `string index out of range`。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### API 功能（本轮新增覆盖）
- 层级：直接父类 `IndexOutOfBoundsException`、可赋值性（RuntimeException/Exception/Serializable）、与 `ArrayIndexOutOfBoundsException` 是兄弟（`Class.isAssignableFrom` 断言非子类型）、公开构造器恰 3 个。
- 构造器矩阵：`()`（null 消息）、`("m")`（逐字 + toString `ClassName: message` + cause null）、`(int)` **直调**（两侧均存在，消息 `String index out of range: n` 对 5/0/-3/Integer.MIN_VALUE 逐字锁定且 **JDK-8 与 JDK-27 逐字相同**）。
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
