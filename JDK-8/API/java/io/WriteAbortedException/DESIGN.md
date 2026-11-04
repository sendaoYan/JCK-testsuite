# java.io.WriteAbortedException 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(String, Exception)`；继承 ObjectStreamException；公共字段 `Exception detail`；getMessage 组合 `"msg; detail"`；getCause()==detail。

## 2. Java 用户如何使用
- 写侧中断后，读侧捕获并检查 detail 取回写侧异常。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/WriteAbortedException.java`。

## 4. 测试用例设计 + 自动化实现（WriteAbortedExceptionTest.java，纯黑盒）
黑盒场景:
1. 构造、detail 字段、组合消息、getCause==detail。
2. 自定义 writeObject 抛异常中止写，读侧包裹原 IOException。
3. 不可序列化成员中止写，读侧包裹 NotSerializableException。
4. 作为 ObjectStreamException 捕获。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 40，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `WriteAbortedException`：双侧同值 `-3326426625597282442L`
### 新增测试方法
1. testMessageComposition（null message→字面「null; ...」；null detail→裸 message；双 null→null；detail 重指派实时改变 getMessage/getCause）
2. testCauseProtocol（构造器已 initCause(null)：任何后续 initCause（含 null detail 实例）恒 ISE；addSuppressed/setStackTrace 正常）
3. testSerializationContract（detail 是 @serial 字段：重指派值也随往返；getCause==detail 恒等式保持）
4. testTriggeredInstance（读侧触发 message 形态 + 双重往返）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- null message + detail → 「null; java.io.IOException: root」
- 读侧触发 → 「writing aborted; java.io.IOException: disk died mid-write」
- initCause 恒 ISE：「Can't overwrite cause with ...」（构造器一次性行为）
- detail 字段 JDK-17 起 @Deprecated（双侧仍公有可用）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
