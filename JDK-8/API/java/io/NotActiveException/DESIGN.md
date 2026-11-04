# java.io.NotActiveException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 ObjectStreamException；表示序列化调用缺少活动上下文。

## 2. Java 用户如何使用
- 仅在 writeObject/readObject 回调内调用 default*/putFields/writeFields/readFields/registerValidation。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/NotActiveException.java`；ObjectStream 类在无上下文时抛出。

## 4. 测试用例设计 + 自动化实现（NotActiveExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 写侧 defaultWriteObject/putFields/writeFields 无上下文抛 NotActiveException。
3. 读侧 defaultReadObject/readFields/registerValidation 无上下文抛 NotActiveException。
4. 回调内部同样调用成功（对比）。
5. 作为 ObjectStreamException 捕获。
- 健壮性: 无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 39，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
