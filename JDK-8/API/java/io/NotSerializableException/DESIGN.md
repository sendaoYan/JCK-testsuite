# java.io.NotSerializableException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 ObjectStreamException；消息为不可序列化类名。

## 2. Java 用户如何使用
- writeObject 遇到不可序列化对象图时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/NotSerializableException.java`；`ObjectOutputStream` 抛出。

## 4. 测试用例设计 + 自动化实现（NotSerializableExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 根对象不可序列化 → 消息为类名。
3. 可序列化类持有不可序列化字段 → 消息为字段类名。
4. 不可序列化父类的可序列化子类可正常序列化（对比）。
5. transient 字段跳过检查（对比）。
6. 作为 ObjectStreamException 捕获且区别于 NotActiveException。
- 健壮性: 无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 37，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `NotSerializableException`：双侧同值 `2906642554793891381L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract
3. testTriggeredMessages（根对象/数组元素/字段/HashMap 值命名规则 + writeReplace 抛 ISE 原样中继 + enum/String 对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- message 即肇事类的完全限定名：根对象 → 「java.lang.Object」
- 数组元素非序列化 → 命名元素类（非数组类）
- 字段非序列化 → 命名字段类型：如「java.lang.Thread」
- writeReplace 抛 IllegalStateException → 原样中继，不产生 NSE

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
