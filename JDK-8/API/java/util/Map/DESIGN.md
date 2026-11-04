# java.util.Map 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。put/get/remove 返回语义、containsKey/containsValue、keySet/values/entrySet、
> 以及 Java 8 默认方法：getOrDefault/putIfAbsent/computeIfAbsent/remove(K,V)/replace(K,V)/
> replace(K,old,new)/computeIfPresent/compute/merge/replaceAll/forEach，含 null 结果移除语义与
> null 参数 NPE。

## 1. API 提供了哪些功能

- **核心**：`put/get/remove`、`containsKey/containsValue`、`keySet/values/entrySet`。
- **默认方法（Java 8）**：`getOrDefault`、`forEach`、`replaceAll`、`putIfAbsent`、`remove(K,V)`、
  `replace(K,V)`、`replace(K,old,new)`、`computeIfAbsent`、`computeIfPresent`、`compute`、`merge`。

## 2. Java 用户如何使用

- `map.getOrDefault(key, d)` 安全读；`map.computeIfAbsent(key, fn)` 惰性初始化；
  `map.merge(key, val, fn)` 原子合并。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Map.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Map.java`（默认方法实现）。

## 4. 测试用例设计（映射到 MapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基本 put/get/remove + 视图 | testBasics + testViews |
| P0 | getOrDefault/putIfAbsent/computeIfAbsent/replaceAll/forEach | testDefaults |
| P0 | remove(K,V)/replace/replace(K,old,new)/computeIfPresent/compute/merge | testDefaultMethodContract |
| P0 | replaceAll/forEach 遍历 | testDefaultReplaceAllForEach |

## 关键规范细节（易错，已锁入断言）

- `remove(K,V)` 仅当当前映射等于 V 时移除并返回 true。
- `replace(K,V)` 仅在 key 存在时替换并返回旧值；`replace(K,old,new)` 仅当当前值等于 old 才替换。
- `computeIfPresent` 结果为 null 时**移除**映射；key 不存在时 no-op。
- `compute` 总是执行 fn；fn 结果为 null 时移除映射。
- `merge(key, value, fn)`：key 不存在时直接存 value；存在时 `fn(old, value)`；结果为 null 时移除。
  `merge` 的 value 为 null 或 fn 为 null 抛 **NPE**；`compute/computeIfAbsent/computeIfPresent/replaceAll`
  的 fn 为 null 抛 **NPE**。

## 注意事项

- 用 HashMap（允许 null）为载体；不锁定迭代顺序。
