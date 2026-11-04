# java.util.AbstractMap.SimpleEntry 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。可变 Map.Entry 实现：getKey/getValue/setValue（返回旧值、连续
> setValue 链式返回前值）、equals 跨类型（与 SimpleImmutableEntry、与 HashMap entry 视图
> 互相 equals）、hashCode 公式 key.hashCode()^value.hashCode() 字面量锁定、null key/value
> 允许、拷贝构造快照语义、toString（含 null 形态）、序列化 round-trip（类还原 + 仍可变）。

## 1. API 提供了哪些功能

- **构造器（2）**：`SimpleEntry(K, V)`、`SimpleEntry(Map.Entry<? extends K, ? extends V>)`
  （拷贝构造，取构造时刻的 key/value 快照）。
- **方法**：`getKey`、`getValue`、`setValue(V)`（返回旧值；不与任何 Map 联动——它只是独立
  entry）、`equals`/`hashCode`/`toString`（"k=v"，null 打印 "null"）。
- **契约**：实现 `Map.Entry`、`Serializable`；null key/value 合法；equals 按 Map.Entry 通用
  公式（key 与 value 分别相等即相等，**与实现类型无关**）；hashCode = key^value。

## 2. Java 用户如何使用

- 临时构造 entry 用于 `Map.entrySet` 交互/自定义 Map 实现的返回值：`new SimpleEntry<>(k, v)`
  → `setValue` 本地生效（返回旧值）→ 作为 `entrySet` 元素或比较单元。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractMap.SimpleEntry.html`：equals/hashCode 公式、
  setValue 语义、可序列化。
- **OpenJDK 源码** `java/util/AbstractMap.java`（SimpleEntry 内部类）。
- 行为差异实测（本轮 javap/运行探针）：equals 与 SimpleImmutableEntry/HashMap entry 视图
  互通；hashCode("k",1)==106。

## 4. 测试用例设计（映射到 SimpleEntryTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getKey/getValue/setValue 返回旧值 | testSimpleEntry（既有） |
| P0 | equals 跨类型（SimpleImmutableEntry/HashMap entry 视图/null/异类型/不同 k/v） | testEqualsContract |
| P0 | hashCode 公式字面量（(k,1)=106、(null,1)=1、(k,null)=107、(null,null)=0） | testHashCodeFormula |
| P1 | 拷贝构造快照（源后续 setValue 不影响副本） | testCopyConstructorSnapshot |
| P1 | setValue 链式返回前值 + toString 含 null 形态 | testSetValueChainAndToString |
| P1 | 序列化 round-trip（类还原、equals、反序列化后仍可变） | testSerialization |

## 关键规范细节（易错，已锁入断言）

- `equals` 与 `SimpleImmutableEntry`/任何 Map.Entry 实现**同 k/v 即相等**（类型无关、对称）。
- `hashCode == key.hashCode() ^ value.hashCode()`；null 按 0 参与异或（字面量 106/1/107/0）。
- 拷贝构造是**快照**：此后源 entry 的 setValue 不影响副本。
- `setValue` 返回**旧值**；连续调用每次返回前一次的值。
- `toString` 形如 "k=v"；null key/value 打印 "null"（"null=null" 合法）。
- 序列化后类还原为 `AbstractMap$SimpleEntry` 且**仍可变**。

## 注意事项

- 无 GC/图形/网络依赖；字面量 106 = "k".hashCode()(107) ^ 1 依赖 String.hashCode 规范
  （稳定规范值，可安全锁定）。
