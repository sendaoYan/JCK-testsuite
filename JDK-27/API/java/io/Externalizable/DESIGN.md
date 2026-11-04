# java.io.Externalizable 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- `writeExternal(ObjectOutput out)`：由实现者**完全掌控**写出的字节（可调 writeInt/writeUTF/writeLong/writeBoolean 等 DataOutput 全套）。
- `readExternal(ObjectInput in)`：按 writeExternal 的逆序读回，自行恢复对象状态。
- 契约：可外部化类必须有 **public 无参构造器**；反序列化时先调它再调 readExternal；serialVersionUID 同样参与版本匹配；transient 不再由机制忽略——一切由作者手写。

## 2. Java 用户如何使用
- `class Msg implements Externalizable { public Msg() {} int i; public void writeExternal(ObjectOutput out){ out.writeInt(i); } public void readExternal(ObjectInput in){ i = in.readInt(); } }`。
- `oos.writeObject(msg)` → 机制写入类描述 + 调用 writeExternal；`ois.readObject()` → 无参构造 + readExternal。
- 与 Serializable 对比：Serializable 自动写全部非 transient 字段；Externalizable 精确手写、流更小、更快，但要求无参构造器。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `class Foo implements Externalizable { ... public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(x); } ... }` — `jdk25u-dev/test/jdk/java/io/Serializable/classDescFlagConflict/Foo.java`（Externalizable 流标志 SC_EXTERNALIZABLE 冲突测试）。
- `jdk25u-dev/test/jdk/java/io/Serializable/classDescHooks/ExternLoopback.java`：writeExternal/readExternal 往返（loopback）用法。
- `jdk25u-dev/test/jdk/java/io/Serializable/InvalidClassException/noargctor/DefaultPackage.java`：无 public 无参构造器 → 反序列化 InvalidClassException("no valid constructor") 的权威触发用例。

## 4. 测试用例设计 + 自动化实现（指向 ExternalizableTest.java 黑盒 `testBlackBox`）
- 白盒: 接口、extends Serializable、两个抽象方法、异常签名、无常量。
- 黑盒场景:
  1. 手写表单完整往返（int/UTF/long/boolean），含"作者故意写 transient 字段"证明机制不干预。
  2. 反序列化确实调用 public 无参构造器（静态计数器）。
  3. 负路径：缺无参构造器 → InvalidClassException("no valid constructor")（message 稳定，精确断言）。
  4. 组合：对象后紧跟原始数据（writeInt）→ readObject 后 readInt 恢复，验证读写对称。
  5. 以 ObjectOutput 视角多态使用。
- 健壮性: 内存流、固定字段值、无时序/全局状态。

产出：`ExternalizableTest.java`（白盒 6 + 黑盒 ~12 断言）。

## 深化追加（契约级）

新增维度（`testInterfaceStructure`…`testExtReplace`，断言 18 → 53）：
- **接口结构**：恰 2 个抽象方法 writeExternal/readExternal；接口自身可序列化（UID -282491828744381764）。
- **构造顺序**：读侧事件序恰为 `["writeExt","ctor","readExt"]` —— public 无参构造器先于 readExternal 执行。
- **字段不自动保存**：ExtFull 无 defaultWriteObject 参与，读回字段为类型默认值（999→0）。
- **无有效构造器**：protected/缺失无参构造器 → InvalidClassException，消息 `endsWith("no valid constructor")`，`ice.classname` 为类名（先实测后写入）。
- **钩子异常传播**：writeExternal 抛 UTFDataFormatException → 原样传出；readExternal 抛 ISE（运行时异常）→ 原样传出不被包装。
- **UID 不匹配**（resolveClass 覆盖法，免双类加载器）：消息同时含 `local class incompatible`、`stream classdesc serialVersionUID = 11`、`local class serialVersionUID = 99`。
- **流内 UID+flags**：手工解析真实字节流 —— TC_OBJECT(0x77→0x73 前导) 后 TC_CLASSDESC(0x72)、suid=11、flags=0x0C（SC_SERIALIZABLE|SC_EXTERNALIZABLE）。
- **全原始面**：byte/short/char '中'/int/long/float/double/boolean/UTF "héllo" 经 writeExternal/readExternal 往返。
- **嵌套对象**：readExternal 内 readObject 可行；短读 → EOFException；回引（构造器仅 1 次，x==y）；writeReplace 同样适用于 Externalizable。

负例矩阵：
| 操作 | 异常 | 消息子串（8/27 一致） |
|---|---|---|
| 缺 public 无参构造器 | InvalidClassException | `no valid constructor`（classname 字段=类名） |
| 流 UID ≠ 本地 UID | InvalidClassException | `local class incompatible` + 两侧 UID 数值 |
| readExternal 短读 | EOFException | — |
| writeExternal 抛受检异常 | 同型异常 | 原消息透传 |

版本差异：无行为差异；JDK-27 侧 POJO 带 @Serial。
