# java.io.ObjectStreamField 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(String name, Class<?> type)`、`(String name, Class<?> type, boolean unshared)`。
- 描述查询：`getName()`、`getType()`、`getTypeCode()`（JVM 签名字母 B/C/D/F/I/J/S/Z/L/[）、`getTypeString()`（基本类型为 null，引用为 JVM 签名）、`isPrimitive()`、`isUnshared()`。
- 偏移：`getOffset()`/`setOffset(int)`（protected setOffset）。
- 排序/展示：`Comparable.compareTo`（基本类型在前，同类别按名字典序）、`toString()`（`<签名> <名字>`）。

## 2. Java 用户如何使用
- 在 `private static final ObjectStreamField[] serialPersistentFields` 中显式声明落盘字段（比 transient 更强的形态控制）。
- 解析/生成类描述符的工具按 `getTypeCode`/`getTypeString` 重建字段表；`compareTo` 用于把字段按线格式规范排序。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new ObjectStreamField("s", String.class), new ObjectStreamField("i", int.class)`（serialPersistentFields 声明形态）— `jdk8u-dev/jdk/test/java/io/Serializable/`（字段表声明用例）。
- `ObjectStreamClass.getFields()` 返回的描述符即本类型 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamClass.java`（字段表构造）。
- compareTo 排序契约 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamField.java` `compareTo` 实现（prim 先、按 name）。

## 4. 测试用例设计 + 自动化实现（ObjectStreamFieldTest.java，纯黑盒）
黑盒场景:
1. int/long/String/int[]/double/unshared 描述符的 name/type/typeCode/typeString/isPrimitive/isUnshared 全套精确断言（typeCode 用 JVM 字母）。
2. offset 初值 0 + 匿名子类调 protected setOffset 生效。
3. compareTo：相等=0、基本<引用、名字序、kind 优先于名字。
4. toString 三形态（`I x`、`Ljava/lang/String; y`、`[I w`）。
5. **真实驱动**：serialPersistentFields 用 ObjectStreamField 数组声明 → 序列化往返只落声明字段（transient 与未声明字段不落盘）。
- 健壮性: 全部固定字面量断言，无环境依赖。

## 深化追加（契约级）

新增维度（`testTypeCodeMatrix`…`testOffsets`，断言 27 → 69）：
- **类型码矩阵**：B/C/D/F/I/J/Z/S/L + `void.class` → 'V'（合法输入，isPrimitive=true）；typeString 对应 null。
- **typeString 矩阵**：`[[I`、`[Ljava/lang/String;`、`[[Ljava/lang/String;`、`[Z` —— **斜杠形式**（非点号），非数组原始类型为 null。
- **compareTo 语义（实测修正）**：先比**种类**（原始 < 引用），再比**名字**；同名不同类型比较结果为 0（不按类型）；`String("a")` vs `int("b")` > 0（种类主导名字）。
- **equals 为同一性语义**：同 name 同 type 的两个 ObjectStreamField **不相等**。
- **toString 形态**：`"Z b"`、`"J j"`（类型码+空格+名）；unshared 与 shared 的 toString 相同。
- **serialPersistentFields 中的 unshared**：描述符含该字段（名字/类型可查），round-trip 后流内无该字段数据。
- **offset**：构造后默认 -1，经 ObjectStreamClass 分配后非负递增。
- **不适用项**：`ObjectStreamField(String,String)` 构造器为**包私有**（非公开 API），黑盒不可达，仅在 DESIGN 记录。

负例矩阵：
| 操作 | 异常 | 消息 |
|---|---|---|
| compareTo(非 ObjectStreamField) | ClassCastException | 8/27 消息文本不同 → 仅断言类型 |

版本差异：compareTo 传非 ObjectStreamField 时的 CCE 消息双侧不同（8: "class ..." vs 27: 措辞变化），故只断言异常类型。
