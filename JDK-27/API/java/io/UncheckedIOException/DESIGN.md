# java.io.UncheckedIOException 测试方案 (DESIGN)

## 1. API 功能
- `(IOException cause)` / `(String message, IOException cause)`：把 IOException 包装为不受检异常；`getCause()` 强类型返回 IOException；cause 不允许 null（requireNonNull）。

## 2. Java 用户如何使用
- 在不允许 throws IOException 的 API（Stream、Runnable、方法引用）里传播 I/O 失败；接收方 catch 后 `getCause()` 解包处理。

## 3. 开源代码实际用法调研
- 类型定义（requireNonNull、getCause 类型化）— `jdk25u-dev/src/java.base/share/classes/java/io/UncheckedIOException.java:49-73`。
- 典型抛出 — `jdk25u-dev/src/java.base/share/classes/java/io/BufferedReader.java`（lines() 内部包装 IOError/UncheckedIOException 路径）与 nio Files 遍历流。

## 4. 测试用例设计 + 自动化实现（UncheckedIOExceptionTest.java）
- 白盒: extends RuntimeException、两构造、getCause 声明返回 IOException。
- 黑盒场景:
  1. (IOException) 构造：message=cause.toString()。
  2. (String, IOException) 构造。
  3. null cause → NPE。
  4. 不受检传播（Runnable 无 throws）。
  5. 真实触发：BufferedReader.lines() 读取中底层 Reader 抛 IOException → 包装为 UIO 且 cause 可解包（两层验证）。
  6. 恢复模式：解包并重抛受检 IOException。
- 健壮性: 自定义 Reader 固定第 2 次调用失败，确定性强；无真实 I/O。

产出：`UncheckedIOExceptionTest.java`（白盒 5 + 黑盒 6 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 44，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `UncheckedIOException`：双侧同值 `-8134305061645241065L`
### 新增测试方法
1. testConstructorRules（(IOException) message==cause.toString() 快照；(null String,cause) 合法；双 null → NPE 无消息；构造器签名反射钉死两个且尾参 IOException）
2. testCauseProtocol（协变 getCause 返回 IOException；initCause 二次/null 均 ISE；suppressed/栈协议）
3. testSerializationContract（message/suppressed 保留，cause 深拷贝非同引）
4. testLinesTriggerDetail（limit(1) 惰性不触发；count() 触发包装；forEach 先交付好元素再抛）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- (null, null) → NullPointerException 且 message 为 null
- lines() 触发实例 cause 即原始 IOException（message 保留）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
