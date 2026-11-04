# java.io.Externalizable 测试方案 (DESIGN)

## 1. API 功能
- 扩展 `Serializable`，由对象完全掌控序列化形式：`writeExternal(ObjectOutput)`、`readExternal(ObjectInput)`。
- 反序列化要求 public 无参构造；`transient` 对 Externalizable 无自动语义。

## 2. Java 用户如何使用
- 手工写 `writeExternal`/`readExternal` 决定每个字段的编码；构造器被反序列化时调用。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamClass.java`（`getExternalizableConstructor` 抛 "no valid constructor"）。

## 4. 测试用例设计 + 自动化实现（ExternalizableTest.java，纯黑盒）
黑盒场景:
1. 手工形式完整往返（int/UTF string/long/boolean + 有意写入的 transient 字段）。
2. 反序列化调用 public 无参构造（实例计数递增）。
3. 无 public 无参构造的 Externalizable 类反序列化抛 InvalidClassException "no valid constructor"。
4. 对象后接原始数据的混合流，readExternal 恰消耗其写出内容。
5. 经 ObjectOutput 接口多态写出。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。

## 深化追加（契约级）

新增维度（`testInterfaceStructure`…`testExtReplace`，断言 12 → 53）：
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
