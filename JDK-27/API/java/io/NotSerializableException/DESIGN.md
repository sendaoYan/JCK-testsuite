# java.io.NotSerializableException 测试方案 (DESIGN)

## 1. API 功能
- `(String classname)` / `()`：writeObject 遇到不可序列化对象时抛出，message 是违规类的名字。

## 2. Java 用户如何使用
- 序列化第三方不可序列化对象时的显式错误；常见修法是把字段标记 transient 或让类实现 Serializable。

## 3. 开源代码实际用法调研
- 抛出点（message=类名）— `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（writeObject0 的 NOT_SERIALIZABLE 分支）。
- 非 Serializable 祖先字段被跳过的规则 — `ObjectStreamClass` 默认字段收集（仅 Serializable 祖先的字段）。

## 4. 测试用例设计 + 自动化实现（NotSerializableExceptionTest.java）
- 白盒: extends ObjectStreamException、两构造。
- 黑盒场景:
  1. 构造器。
  2. 真实触发 1：根对象非 Serializable → message 为其类名。
  3. 真实触发 2：Serializable 类的字段值非 Serializable → message 为字段类名。
  4. 规则对照：实现 Serializable 的"普通父类子类"可序列化（祖先字段跳过，不抛）。
  5. transient 字段逃过检查。
  6. 可按 ObjectStreamException 捕获且与 NotActiveException 区分。
- 健壮性: 全内存。

产出：`NotSerializableExceptionTest.java`（白盒 4 + 黑盒 7 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 40，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
