# java.util.EnumMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`EnumMap(Class<K>)`、`EnumMap(EnumMap<K,? extends V>)`、`EnumMap(Map<K,? extends V>)`。
- 核心操作：`put`/`get`/`remove`/`clear`/`putAll`。
- 查询：`size`/`isEmpty`/`containsKey`/`containsValue`。
- 视图：`keySet`/`values`/`entrySet`（迭代按枚举声明序）。
- 契约/转换：`equals`/`hashCode`/`clone`；继承默认方法 `getOrDefault`/`putIfAbsent`/`replace`/`compute*`/`merge`/`replaceAll`/`forEach`。

## 2. Java 用户如何使用

- 构造：`new EnumMap<>(Color.class)`；`new EnumMap<>(srcEnumMap)`；`new EnumMap<>(srcMap)`（非空 Map）。
- 典型调用：`put(Color.BLUE, "blue")`；`get`/`remove`；`keySet()` 迭代恒为枚举声明序。
- 真实组合链路：
  1. 乱序 put 多键 → keySet/values 按声明序迭代。
  2. get/put 更新/remove。
  3. 默认方法（putIfAbsent/getOrDefault/replace/compute/merge/replaceAll/forEach）。
  4. 构造器拷贝（EnumMap/Map）与 clone 的独立性与声明序保持。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/EnumMap/EnumMapBash.java:61/85/127/130`：`new EnumMap<>(enumClass)`、`new EnumMap<>(m)`、`new EnumMap<>((Map)m)` —— 覆盖三种构造器拷贝路径。
- JDK 测试 `test/jdk/java/util/EnumMap/DistinctEntrySetElements.java:44`：`new EnumMap<>(TestEnum.class)` 验证 entrySet 元素互异。

## 4. 测试用例设计 + 自动化实现

实现位置：`EnumMapTest.java`（`testWhiteBox()` + `testOrderAndOps()`/`testNullHandling()`/`testDefaultMethods()`/`testConstructorsAndClone()`/`testViews()`）。

- 正常：枚举声明序迭代、put/get/remove。
- 组合：默认方法 + 构造器拷贝 + clone 独立性 + 视图联动。
- 边界/异常：put(null) 抛 NPE；get/remove/containsKey(null) 优雅返回 null/false；null value 允许；`EnumMap(空 Map)` 抛 IAE。
- 健壮性：确定性（固定枚举）；无全局副作用；无文件/网络；跨平台。headless-safe。

## 深化追加（契约级，第二轮，JDK-27）

新增 testComputeMergeMatrix / testMappedToNullBranches / testWrongKeyType /
testIteratorsNotFailFast / testBulkViewOps / testEmptyAndRoundTripOrder：

- **compute-merge 全分支矩阵**：putIfAbsent/remove(k,v)/replace 两形态/getOrDefault/
  computeIfAbsent/computeIfPresent/compute/merge 全分支 + 计数器证明 present 不调函数 +
  **异常同实例透传** + null fn/null merge value NPE。
- **mapped-to-null 专属分支**：键映射到 null 时 get()==null 但 containsKey==true：
  computeIfAbsent 仍调用函数并覆盖；remove(k, null) 成功删除；replace(k, null, v) 成功；
  merge 把 mapped-to-null 视为 absent（直接放 value、不调函数，计数器证明）。
- **键类型**：put(wrong type key) → ClassCastException（get/containsKey 对错误类型优雅返回
  null/false，不抛）。
- **迭代器非 fail-fast（与其它 Map 相反）**：EnumMap 迭代器按 ordinal 下标推进，迭代中插入
  更高 ordinal 键**不抛 CME 且新键被后续 next 看到**——确定性断言。
- **视图批量操作**：keySet containsAll/retainAll/removeAll 反映回 map 且保持声明序；
  putAll(HashMap) 合并。
- **空表序列化 + 乱序插入 round-trip 后仍按 ordinal 序迭代**。
