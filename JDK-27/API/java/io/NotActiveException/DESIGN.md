# java.io.NotActiveException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `()`：序列化上下文未激活时调用了需要上下文的方法（defaultWriteObject/putFields/writeFields 在 writeObject 之外；defaultReadObject/readFields/registerValidation 在 readObject 之外）。

## 2. Java 用户如何使用
- 在自定义 writeObject/readObject 之外的代码误用上述 API 时的错误信号。

## 3. 开源代码实际用法调研
- 触发点 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（defaultWriteObject/putFields/writeFields 的 noCurrentObject 检查）与 `ObjectInputStream.java`（defaultReadObject/readFields/registerValidation）。
- 官方行为测试 — `jdk25u-dev/test/jdk/java/io/Serializable/` 各 writeObject/readObject 用例。

## 4. 测试用例设计 + 自动化实现（NotActiveExceptionTest.java）
- 白盒: extends ObjectStreamException、两构造。
- 黑盒场景:
  1. 构造器。
  2. 写侧 3 个触发（defaultWriteObject/putFields/writeFields 在 writeObject 外调用）。
  3. 读侧 3 个触发（defaultReadObject/readFields/registerValidation）。
  4. 对照：同样的调用在自定义 writeObject/readObject 内部成功往返。
  5. 可按 ObjectStreamException 捕获。
- 健壮性: 全内存流，无全局状态。

产出：`NotActiveExceptionTest.java`（白盒 4 + 黑盒 10 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 42，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `NotActiveException`：双侧同值 `-3893467273049808895L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract
3. testTriggeredMessage（六个真实触发点 + 精确 message）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- registerValidation 脱离上下文 → 「stream inactive」
- readFields/defaultReadObject 脱离 readObject → 「not in call to readObject」
- putFields/defaultWriteObject 脱离 writeObject → 「not in call to writeObject」
- writeFields 无 PutField → 「no current PutField object」

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
