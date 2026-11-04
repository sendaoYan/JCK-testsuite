# java.io.WriteAbortedException 测试方案 (DESIGN)

## 1. API 功能
- `(String detail, Exception ex)`：写侧异常中止了序列化，读侧读该流时抛出；公共字段 `detail` 保存原始写侧异常；`getMessage()` = `detail message + "; " + detail`；`getCause()` 返回 detail。

## 2. Java 用户如何使用
- 读一个写入中途失败的流（写方 writeObject 抛过异常但流已产生部分数据）时，读方得到本异常并可取 detail 定位写侧根因。

## 3. 开源代码实际用法调研
- 抛出点 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（读 TC_EXCEPTION 时恢复写侧异常表）。
- 写侧记录 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（writeObject 异常写入 TC_EXCEPTION/bout）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Serializable/writeAbortedException/`。

## 4. 测试用例设计 + 自动化实现（WriteAbortedExceptionTest.java）
- 白盒: extends ObjectStreamException、public detail、getMessage/getCause 重写、单构造。
- 黑盒场景:
  1. 构造：组合 message、detail 字段、getCause==detail。
  2. 真实触发 1：自定义 writeObject 抛 IOException → 写侧原样传播，读侧抛 WAE 且 detail 为原异常。
  3. 真实触发 2：非 Serializable 字段毒化流 → 读侧 WAE.detail 为 NotSerializableException。
  4. 可按 ObjectStreamException 捕获。
- 健壮性: 全内存流。

产出：`WriteAbortedExceptionTest.java`（白盒 8 + 黑盒 7 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 46，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
