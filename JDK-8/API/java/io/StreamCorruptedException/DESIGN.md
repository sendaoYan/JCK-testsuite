# java.io.StreamCorruptedException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 ObjectStreamException；表示序列化流损坏。

## 2. Java 用户如何使用
- ObjectInputStream 读取坏流时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/StreamCorruptedException.java`；ObjectInputStream 头部/标签校验抛出。

## 4. 测试用例设计 + 自动化实现（StreamCorruptedExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 无效流头 → "invalid stream header"。
3. 截断头 → StreamCorruptedException 或 EOFException。
4. 合法头后未知标签 → "invalid type code"。
5. 手工破坏载荷 → 值改变或流/解码异常。
6. 未破坏流干净往返（对比）。
7. 作为 ObjectStreamException 捕获。
- 健壮性: 确定性字节；无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 36，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
