# java.util.AbstractMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。`SimpleEntry`（getKey/getValue/setValue 返回旧值/equals/hashCode/toString/
> Map.Entry 拷贝构造/null 允许）、`SimpleImmutableEntry`（setValue 抛 UOE/equals/hashCode/toString）、
> `AbstractMap` equals/hashCode（顺序无关）/toString、默认 `put` 抛 UOE、entrySet/keySet/values 视图、
> remove。

## 1. API 提供了哪些功能

- **抽象方法**：`entrySet()`。
- **默认实现**：`get`、`put`（UOE）、`remove`、`containsKey`、`containsValue`、`keySet`、`values`、
  `equals`、`hashCode`、`toString`、`clone`（protected）。
- **嵌套类**：`SimpleEntry`（可变）、`SimpleImmutableEntry`（不可变）。

## 2. Java 用户如何使用

- 自定义 Map 只需实现 `entrySet()`；`SimpleEntry`/`SimpleImmutableEntry` 作为通用 Entry 实现。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractMap.html`、`AbstractMap.SimpleEntry`、
  `AbstractMap.SimpleImmutableEntry`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/AbstractMap.java`
  （默认 `put` 抛 UOE；`SimpleEntry` 不判空、允许 null key/value；`SimpleImmutableEntry.setValue` 抛 UOE）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/AbstractMap`。

## 4. 测试用例设计（映射到 AbstractMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | SimpleEntry 全套（含 null 允许） | testSimpleEntry |
| P0 | SimpleImmutableEntry（setValue UOE） | testSimpleImmutableEntry |
| P0 | 默认 put 抛 UOE | testDefaultPutUOE |
| P0 | AbstractMap equals/hashCode/toString + 视图 | testAbstractMapBehavior |

## 关键规范细节（易错，已锁入断言）

- **`SimpleEntry(null, null)` 允许**（构造器不判空），`equals`/`hashCode` 正确处理 null。
- `SimpleEntry.setValue` 返回旧值并更新；`SimpleImmutableEntry.setValue` 抛 **UOE**。
- 两者都支持 `Map.Entry` 拷贝构造。
- 默认 `put`/`remove` 抛 **UOE**（只实现 `entrySet()` 的只读 Map）。
- `AbstractMap.equals`/`hashCode` 与迭代顺序无关（Map 契约）；`toString` 形如 `{k=v, ...}`。

## 注意事项

- 无 GC/图形/网络/文件依赖；`toString` 断言不锁定多条目顺序。
