# java.io.Serializable 测试方案 (DESIGN)

## 1. API 功能
- 标记接口（无方法、无常量）：使类的实例可被 `ObjectOutputStream` 序列化、`ObjectInputStream` 反序列化。
- 数组类型一律 Serializable；`Externalizable` 扩展 Serializable。

## 2. Java 用户如何使用
- `class X implements Serializable` + `serialVersionUID`；非序列化字段加 `transient`；对象图可写读往返。

## 3. 开源代码实际用法调研
- 语义 — `jdk8u-dev/jdk/src/share/classes/java/io/Serializable.java`、`ObjectOutputStream/ObjectInputStream`。

## 4. 测试用例设计 + 自动化实现（SerializableTest.java，纯黑盒）
黑盒场景:
1. JDK 类型可序列化性判定（String/Integer/ArrayList/LocalDate 是，Scanner/Thread/Object 否）。
2. 数组类型（int[]/Object[]/int[][]）皆 Serializable。
3. String 与 int[] 经标记契约往返；普通 Object 写抛 NotSerializableException 且消息为类名。
4. 枚举常量往返且同一性保持。
5. 用户标记类、无字段类、继承的 Serializable 子类往返。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。

## 深化追加（契约级）

新增维度（`testMarkerStructure`…`testArrayMatrix`，断言 22 → 57）：
- **标记接口结构**：Serializable 无任何方法/常量，仅作能力标记；`ObjectStreamClass.lookup` 对其返回 null。
- **NotSerializableException 精确消息**：写出非序列化类抛出的消息 == 该类二进制名（`endsWith` 校验）。
- **钩子签名匹配计数器**：`HookOk`（private void writeObject/readObject(ObjectOutputStream/ObjectInputStream)）计数递增；`HookWrongParam`（参数 DataOutput）、`HookWrongAccess`（public）、`HookWrongReturn`（非 void）三者的钩子被**静默忽略**，走默认序列化（计数为 0）。
- **transient/static 语义**：transient 字段读回默认值；static 字段在写读之间被修改后读回**新值**（static 不属于对象状态）。
- **字节确定性**：同内容对象两次序列化 `Arrays.equals` 逐字节相等。
- **子接口继承**：`MarkerIface extends Serializable` 的实现类可序列化。
- **writeReplace/readResolve**：替换对象进流（读回 "replaced-1"）；readResolve 返回哨兵保持同一性；替换结果非 Serializable → NotSerializableException 消息含 `java.lang.Object`。
- **图形状**：同对象两次写出 → 第二次为回引（`==` 同一性）；环形引用可完整往返。
- **数组矩阵**：int[]/String[]/Object[][]/枚举数组长度与元素类型保持。

负例矩阵：
| 操作 | 异常 | 消息子串（8/27 一致） |
|---|---|---|
| writeObject(非序列化对象) | NotSerializableException | 类二进制名 |
| writeReplace 返回非序列化对象 | NotSerializableException | `java.lang.Object` |

版本差异：JDK-27 侧 POJO 的 serialVersionUID 追加 `@Serial`（编译期校验），JDK-8 侧无该注解；其余行为双侧一致。
