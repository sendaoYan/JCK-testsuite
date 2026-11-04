# java.util.HashMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`HashMap()`、`HashMap(int initialCapacity)`、`HashMap(int, float loadFactor)`、`HashMap(Map<? extends K,? extends V> m)`。
- 核心操作：`put(K,V)`、`get(Object)`、`remove(Object)`、`remove(Object,Object)`、`putAll(Map)`、`clear()`。
- 查询：`size()`、`isEmpty()`、`containsKey(Object)`、`containsValue(Object)`。
- 视图：`keySet()`、`values()`、`entrySet()`。
- 转换/默认方法：`getOrDefault`、`putIfAbsent`、`replace(K,V)`、`replace(K,old,new)`、`replaceAll`、`computeIfAbsent`、`computeIfPresent`、`compute`、`merge`、`forEach`。
- 契约方法：`equals`、`hashCode`。

## 2. Java 用户如何使用

- 构造：`Map<String,Integer> m = new HashMap<>();` 或 `new HashMap<>(64, 0.75f)`（预分容量），或 `new HashMap<>(srcMap)` 拷贝。
- 典型调用：
  - 计数/缓存：`m.computeIfAbsent(key, k -> init(k))`；`m.merge(word, 1, Integer::sum)` 做词频统计。
  - 缺省：`m.getOrDefault(k, 0)`；`m.putIfAbsent(k, v)`。
  - 批量：`m.replaceAll((k,v) -> v.trim())`；`m.forEach((k,v) -> ...)`。
- 真实组合链路：
  1. `put` 插入多键（含 null key/value）→ `get/containsKey/containsValue` 校验。
  2. `replace`/`replaceAll` 更新 → `remove(K)`/`remove(K,V)` 删除。
  3. `putIfAbsent`/`getOrDefault` → `computeIfAbsent`/`computeIfPresent`/`compute`/`merge`（注意返回 null 的删除语义）。
  4. `keySet()/values()/entrySet()` 视图修改联动 → 结构修改后迭代 fail-fast（CME）。
  5. `equals`/`hashCode`/`clear`/`forEach` 收尾。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.Collections`（包装类委托默认方法）：
  `Collections.java:1705-1707` `getOrDefault` 委托；`Collections.java:1741` `computeIfAbsent` 委托；
  `Collections.java:3092` `merge` 委托；`Collections.java:4132-4135` `computeIfAbsent` 带 `synchronized`。
  说明同步/不可变包装要转发所有默认方法，否则绕过包装约束。
- JDK 测试 `test/jdk/java/util/SequencedCollection/BasicMap.java:673-697`：
  对 `map.reversed()` 视图依次调用 `putIfAbsent("x",99)`、`computeIfAbsent("x", k->99)`、`merge("x",99,(k,v)->-1)`，
  验证视图与默认方法的联动。
- JDK 测试 `test/jdk/java/util/Properties/CheckOverrides.java:52`：
  `unoverriddenMethods.putIfAbsent(new MethodSignature(m), m)` —— 用 `putIfAbsent` 去重收集方法签名。

## 4. 测试用例设计 + 自动化实现

实现位置：`HashMapTest.java`（`testWhiteBox()` 白盒 + `testContractChain()`/`testNullSemantics()`/`testViewsAndFailFast()`/`testEqualsHashCodeClearForEach()` 黑盒）。

- 正常链路：完整 put→get/containsKey/containsValue→replace/replaceAll→remove→putIfAbsent/getOrDefault→compute*/merge→size/isEmpty→视图→equals/hashCode→clear→forEach（组合操作，非单方法断言）。
- 组合链路：默认方法返回 null 的删除/不插入语义（compute/merge 返回 null 删除，computeIfAbsent 返回 null 不插入）。
- 边界/异常：null key/value 允许；视图迭代器结构修改抛 `ConcurrentModificationException`。
- 健壮性：无墙钟/无随机/无 HashMap 迭代顺序断言（不依赖顺序）；无全局副作用；无文件/网络/端口；跨平台无路径/编码依赖。headless-safe，不加 `@key`。
