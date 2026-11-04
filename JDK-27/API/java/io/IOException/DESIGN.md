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

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 91，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `IOException`：双侧同值 `7818375828146090155L`
### 新增测试方法
1. testConstructorEdges（cause 提取规则：(Throwable)→message==cause.toString()；(String,null)→cause 为 null）
2. testThrowableProtocol（getMessage/getLocalizedMessage/toString/initCause 二次 ISE/initCause(null) 仅未设置时允许/setStackTrace 往返+null NPE/addSuppressed 顺序+自抑制 IAE+null NPE/fillInStackTrace==this）
3. testSerializationContract（suid + 消息/cause/suppressed 往返）
4. testFamilyHierarchy（15 个 java.io 异常家族 instanceof 矩阵）
5. testRealThrowSurfaces（EOFException/FNFE 等真实抛出可作 IOException 捕获）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- initCause 二次 → IllegalStateException「Can't overwrite cause with ...」
- initCause(this) → IllegalArgumentException「Self-causation not permitted」（在 Throwable 基类层面）
- addSuppressed(this) → IllegalArgumentException「Self-suppression not permitted」
- setStackTrace(null) → NullPointerException

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
