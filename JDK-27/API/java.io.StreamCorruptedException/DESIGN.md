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
