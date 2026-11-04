# java.util.AbstractList 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。抽象 `get(int)`/`size()`、默认 `add(E)`/`add(int,E)`/`set`/`remove(int)`
> 抛 UOE、`indexOf`/`lastIndexOf`（equals 语义，含 null）、`subList` live 视图 + 边界（IAE/IOBE）、
> `equals`/`hashCode`（List 契约，跨实现 equals）、`removeRange`（protected，子类暴露）。

## 1. API 提供了哪些功能

- **抽象方法**：`get(int)`、`size()`。
- **默认实现**：`add(E)`（委托 `add(size, e)`）、`add(int,E)`（UOE）、`set`（UOE）、`remove(int)`（UOE）、
  `indexOf`/`lastIndexOf`、`subList`、`equals`/`hashCode`、`iterator`/`listIterator`、`removeRange`（protected）。

## 2. Java 用户如何使用

- 自定义列表只需实现 `get(int)` + `size()`；可变列表再覆写 `add/set/remove`。
- 只读列表不覆写位置写方法 → 调用抛 UOE。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractList.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/AbstractList.java`
  （默认 `add(int,E)`/`set`/`remove(int)` 抛 UOE；`subList` 范围检查 from<0/from>to/from>size）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/AbstractList`。

## 4. 测试用例设计（映射到 AbstractListTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认 add/set/remove 抛 UOE | testDefaultOpsUOE |
| P0 | indexOf/lastIndexOf（含 null） | testIndexOfLastIndexOf |
| P0 | subList live 视图 + 边界 | testSubListView |
| P0 | equals/hashCode 跨实现 | testEqualsHashCode |
| P0 | removeRange（protected） | testRemoveRange |

## 关键规范细节（易错，已锁入断言）

- 默认 `add(E)` 委托 `add(size(), e)`；`add(int,E)`/`set`/`remove(int)` 默认抛 **UOE**。
- `indexOf`/`lastIndexOf` 用 `equals`（null 用 `==`），找不到返回 -1。
- `subList(from,to)`：`from>to` 抛 **IllegalArgumentException**；`from<0` 或 `to>size` 抛
  **IndexOutOfBoundsException**；是 live 视图（双向联动）。
- `removeRange` 是 protected，测试通过子类暴露方法验证 `[from,to)` 移除、`from==to` 为 no-op。

## 注意事项

- 无 GC/图形/网络/文件依赖；不测试 fail-fast（需正确维护 modCount 的自定义子类，已由 ArrayList/List 覆盖）。
