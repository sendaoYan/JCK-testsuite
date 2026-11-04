# java.util.concurrent.ConcurrentMap 设计案

## 1. API 功能
线程安全 Map 接口（继承 Map）。原子 `putIfAbsent`/`remove(k,v)`/`replace` 以及 `compute*`/`merge`/`getOrDefault` 默认方法。

## 2. Java 用户如何使用
并发读写 Map：`putIfAbsent` 无锁插入、`computeIfAbsent` 惰性初始化、`merge` 聚合计数。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ConcurrentHashMap` 实现；`test/jdk/java/util/concurrent/ConcurrentMap/` 与 tck 覆盖。

## 4. 测试用例设计 + 自动化实现（契约级深化）
文件：`ConcurrentMapTest.java`（白盒 + 黑盒合并，运行时 121 断言；深化前 16）。在原有结构白盒 + 冒烟黑盒之上追加契约维度：

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testWhiteBox（保留+深化） | 原方法面核对保留；追加：唯一父接口恰为 Map、putIfAbsent/remove(k,v)/replace×2 为 abstract 且 declaringClass=ConcurrentMap、getOrDefault/forEach/replaceAll/computeIfAbsent/computeIfPresent/compute/merge 七方法 isDefault 且由本接口声明、类型参数 K/V | P0 |
| 2 | testBlackBox（原冒烟保留） | 原子操作主干 + compute/merge 基本路径 | P0 |
| 3 | testAtomicBranchMatrix | ConcurrentHashMap 驱动全分支：putIfAbsent absent→null+插入/present→返回现存值不覆盖/删除后再插；remove(k,v) 键缺席/值不符 false/匹配 true；replace(k,v) 缺席→null 且不插入/present→返回旧值；replace(k,o,n) 键缺席/旧值过期（刚被覆盖）/匹配；equals 匹配（等值不同对象） | P0 |
| 4 | testMinimalDefaults + MinimalConcurrentMap | 自造最小实现（仅委托 ConcurrentHashMap，**不覆写任何方法**）验证继承的 ConcurrentMap 默认方法：getOrDefault 两态、forEach 全量+空 map 零调用、computeIfAbsent absent 调一次/present 函数零调用（计数器）/null 结果不落、computeIfPresent absent 零调用/present/null 删、compute absent/present/null 删、merge absent 函数零调用直存/present (old,value)/null 删、replaceAll 全量改写；异常中继：**同一异常实例**透传 + 映射不变；null 函数 NPE | P0 |
| 5 | testHierarchyAndGenerics | instanceof 链、isAssignableFrom、Map/wildcard 上转型操作、跨实现 equals/hashCode（Map 契约）、entrySet 活视图 | P1 |

易错规范细节（实测锁定，JDK-27 与 JDK-8 一致）：
- `putIfAbsent` present 返回现存值；`replace(k,v)` 缺席返回 null 且不插入。
- ConcurrentMap 默认方法（JDK-27 javap 标注 `default`）在最小实现上呈现「present 不调函数」「merge absent 不经函数」的可观察契约。
- merge absent 时提供的 value 直接落表（函数零调用）；compute/merge 返回 null → 删除映射。
- equals 匹配语义：`remove(k,v)`/`replace(k,o,n)` 用 equals 而非 ==。

优先级：P0 = 原子分支矩阵 + 默认方法行为 + 异常中继；P1 = 结构 + 层次泛型。

断言数：121（运行时 `check` 计数）。
