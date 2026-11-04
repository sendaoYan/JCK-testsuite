# java.io.UncheckedIOException 测试方案 (DESIGN)

## 1. API 功能
- `(IOException)` 与 `(String, IOException)` 构造；继承 RuntimeException；getCause 协变返回 IOException。

## 2. Java 用户如何使用
- 在不能抛受检异常的流式 API（如 BufferedReader.lines()）中包装 IOException。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/UncheckedIOException.java`。

## 4. 测试用例设计 + 自动化实现（UncheckedIOExceptionTest.java，纯黑盒）
黑盒场景:
1. `(IOException)`：cause 保留、消息=cause.toString()。
2. `(String, IOException)`：消息 + cause。
3. null cause 拒收 NPE。
4. 未检查传播。
5. BufferedReader.lines() 中途 IOException 包装为 UncheckedIOException。
6. 捕获后解包恢复受检 IOException。
- 健壮性: 无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 40，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
