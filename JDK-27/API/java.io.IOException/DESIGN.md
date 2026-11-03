# java.io.IOException 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- `IOException()` / `(String)` / `(String, Throwable)` / `(Throwable)`：四个公共构造，最后一个的 message 默认为 `cause.toString()`。
- 继承自 `Exception`（受检异常），是 java.io 全部流/序列化异常的根；继承 `Throwable` 的 `initCause/addSuppressed/getSuppressed` 行为。

## 2. Java 用户如何使用
- 所有 java.io 流方法 `throws IOException`，调用方 try/catch 或上抛；自定义 I/O 层异常通常继承它。
- JDK 7 起 try-with-resources 的 close 异常可 `addSuppressed` 附加到主异常上。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 构造与 cause/message 组合语义 — `jdk25u-dev/src/java.base/share/classes/java/io/IOException.java`。
- suppressed/cause 组合行为 — `jdk25u-dev/test/jdk/java/io/Serializable/` 各测试与 `Throwable` 契约测试。

## 4. 测试用例设计 + 自动化实现（指向 IOExceptionTest.java）
- 白盒: extends Exception、非 RuntimeException、四个公共构造精确计数、serialVersionUID 字段存在。
- 黑盒场景:
  1. 四构造 message/cause 矩阵（(Throwable) 的 message == cause.toString()）。
  2. initCause：一次性、返回 this、`initCause(this)` → IAE、未初始化时可接受任意非自身 cause。
  3. addSuppressed 顺序保留。
  4. 异常本身可序列化且 message 保留。
  5. 真实 API 抛出的 EOFException 可按 IOException 捕获。
  6. try-with-resources 兼容。
  7. toString/stackTrace 行为。
- 健壮性: 全内存对象与流，无文件/网络/全局状态。

产出：`IOExceptionTest.java`（白盒 6 + 黑盒 15 断言）。
