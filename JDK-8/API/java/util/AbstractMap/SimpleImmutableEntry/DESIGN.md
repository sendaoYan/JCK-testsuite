# java.util.AbstractMap.SimpleImmutableEntry 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。不可变 Map.Entry：setValue → UnsupportedOperationException 且值
> 不变（含反序列化后）、equals 跨类型（与 SimpleEntry、与 HashMap entry 视图互相 equals）、
> hashCode 公式 key.hashCode()^value.hashCode() 字面量锁定、null key/value 允许、拷贝构造
> 快照（源可变 entry 后续 setValue 不影响副本）、toString 含 null 形态、序列化 round-trip
> （类还原 + 仍不可变）。

## 1. API 提供了哪些功能

- **构造器（2）**：`SimpleImmutableEntry(K, V)`、`SimpleImmutableEntry(Map.Entry)`（拷贝构造，
  取构造时刻快照）。
- **方法**：`getKey`、`getValue`（只读）、`setValue` → UnsupportedOperationException、
  `equals`/`hashCode`/`toString`（"k=v"）。
- **契约**：实现 `Map.Entry`、`Serializable`；null key/value 合法；equals/hashCode 与
  Map.Entry 通用契约一致（类型无关）。

## 2. Java 用户如何使用

- 为不可变映射/线程安全快照构造 entry：`new SimpleImmutableEntry<>(k, v)` → 任何 setValue 均
  抛 UOE → 安全跨线程传递。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/AbstractMap.SimpleImmutableEntry.html`：setValue UOE、
  equals/hashCode 公式、可序列化。
- **OpenJDK 源码** `java/util/AbstractMap.java`（SimpleImmutableEntry 内部类）。
- 行为实测（本轮探针）：equals 与 SimpleEntry/HashMap entry 视图互通；hashCode("k",1)==106。

## 4. 测试用例设计（映射到 SimpleImmutableEntryTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | setValue → UOE 且值不变 | testSimpleImmutableEntry（既有）、testImmutability |
| P0 | equals 跨类型/null/异类型/不同 k/v | testEqualsContract |
| P0 | hashCode 公式字面量（(k,1)=106、(null,1)=1、(k,null)=107、(null,null)=0） | testHashCodeFormula |
| P1 | 拷贝构造快照（可变源后续 setValue 不影响副本） | testCopyConstructorSnapshot |
| P1 | 序列化 round-trip（类还原、equals、反序列化后仍不可变） | testSerialization |

## 关键规范细节（易错，已锁入断言）

- `setValue` 抛 UnsupportedOperationException 且 **value 保持不变**（含反序列化实例）。
- `hashCode == key.hashCode() ^ value.hashCode()`（null 按 0；字面量 106/1/107/0）。
- equals 与 `SimpleEntry`/任何 Map.Entry 实现**同 k/v 即相等**（类型无关、对称）。
- 拷贝构造是快照：可变源 entry 之后 setValue 不影响本不可变副本。

## 注意事项

- 无 GC/图形/网络依赖；字面量 106 = "k".hashCode()(107) ^ 1（String.hashCode 规范稳定值）。
