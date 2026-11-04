# java.io.StreamCorruptedException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `()`：流内部控制信息损坏（魔数/版本头错误、未知 TC_* 标签、非法 type code）。

## 2. Java 用户如何使用
- 读取被截断、篡改或非序列化数据的 ObjectInputStream 时捕获。

## 3. 开源代码实际用法调研
- "invalid stream header" — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（readStreamHeader）。
- "invalid type code" — ObjectInputStream.readObject0 的标签分发与 readString/readObject0 对未知码的处理。
- 官方负例测试 — `jdk25u-dev/test/jdk/java/io/Serializable/corruptedStreams/`、`jdk25u-dev/test/jdk/java/io/StreamCorruptedException/`。

## 4. 测试用例设计 + 自动化实现（StreamCorruptedExceptionTest.java）
- 白盒: extends ObjectStreamException、两构造。
- 黑盒场景:
  1. 构造器。
  2. 真实触发 1：4 字节垃圾头 → "invalid stream header"。
  3. 真实触发 2：仅 STREAM_MAGIC 两字节（截断头）→ SCE 或 EOF。
  4. 真实触发 3：合法头 + 未知标签 0x7F → "invalid type code"。
  5. 位翻转载荷 → SCE/UTFDF/EOF 之一（损坏必然显形）。
  6. 对照：未动流正常往返。
  7. 可按 ObjectStreamException 捕获。
- 健壮性: 手工构造字节流，确定性。

产出：`StreamCorruptedExceptionTest.java`（白盒 3 + 黑盒 8 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 39，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `StreamCorruptedException`：双侧同值 `8983558202217591746L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract（含触发实例再序列化）
3. testDiagnostics（垃圾头/版本错/tag 0x7F 精确诊断 + 空流 EOFException 对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 垃圾头 00 01 02 03 → 「invalid stream header: 00010203」（十六进制逐字节）
- STREAM_MAGIC 对但版本错 → 「invalid stream header: ...」
- 合法头后 0x7F tag → 「invalid type code: 7F」
- 空流（0 字节）→ EOFException 而非 SCE（负例对照）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
