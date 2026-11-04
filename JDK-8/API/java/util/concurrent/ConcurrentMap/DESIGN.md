# java.util.concurrent.ConcurrentMap 设计案

## 1. API 功能
线程安全 `Map` 接口（直接父接口恰为 `java.util.Map`，泛型参数 K/V）。核心契约是**内存一致性 + 原子性**：
- 四个原子操作（接口内重声明为 abstract，语义为「单原子步骤」）：`putIfAbsent(K,V)`、`remove(Object,Object)`、`replace(K,V,V)`、`replace(K,V)`；
- 七个默认方法（ConcurrentMap 自身覆写 Map 的默认实现，用 get/putIfAbsent/replace 循环改写为无死锁、保证不回退的重试形态）：`getOrDefault`、`forEach`、`replaceAll`、`computeIfAbsent`、`computeIfPresent`、`compute`、`merge`。
- 匹配语义：`remove(k,v)`/`replace(k,old,new)` 的旧值比较用 `equals` 而非 `==`；旧值不符时整体不做任何事（返回 false）。

## 2. Java 用户如何使用 + 开源实际用法调研
- 并发读写共享状态：`putIfAbsent` 实现无锁「没有则初始化」（如缓存首写）、`computeIfAbsent` 惰性初始化 per-key 缓存、`merge` 原子计数（词频表 `merge(k,1,Integer::sum)`）、`replace(k,old,new)` 乐观并发控制（CAS 语义）。
- 开源用法：JDK 内部 `ConcurrentHashMap`/`ConcurrentSkipListMap` 均实现本接口；Netty 的内部并发缓存、Guava `MapMaker` 的弱一致性 Map、Kafka/Cassandra 的 per-partition 状态表大量使用 `putIfAbsent` 做幂等注册。
- OpenJDK `test/jdk/java/util/concurrent/`（ConcurrentMap tck 系列）以「分支矩阵 + 函数副作用计数器」方式验证默认方法。

## 3. 参考来源
- Oracle javadoc `java.util.concurrent.ConcurrentMap`（内存一致性保证：先 happen-before 于后继的 get/putIfAbsent/…）。
- OpenJDK 8 `ConcurrentMap.java` 默认方法源码（compute/merge 的重试循环形态）。
- `~/git/jdk8u-dev`、`~/git/jdk` 的 `test/jdk/java/util/concurrent/ConcurrentMap/`。
- 仓库内已深化范例 `JDK-8/API/java/util/concurrent/ConcurrentHashMap/ConcurrentHashMapTest.java`。

## 4. 测试用例设计 + 自动化实现
文件：`ConcurrentMapTest.java`（白盒 + 黑盒合并，运行时 108 断言；基线 7）。

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testConcurrentMap（原冒烟保留） | putIfAbsent/replace/remove 主干 | P0 |
| 2 | testWhiteBox | isInterface、唯一父接口 Map、四原子方法 abstract 且由本接口声明、七默认方法 isDefault 且 declaringClass=ConcurrentMap、类型参数 K/V | P0 |
| 3 | testAtomicBranchMatrix | ConcurrentHashMap 驱动全分支：putIfAbsent absent→null+插入 / present→返回旧值不覆盖；remove(k,v) 键缺席/值不符/匹配；replace(k,v) 缺席→null 且不插入 / present→返回旧值；replace(k,o,n) 键缺席/旧值过期(刚被覆盖)/匹配；equals 匹配（等值不同对象） | P0 |
| 4 | testMinimalDefaults | 自造 `MinimalConcurrentMap`（仅委托 ConcurrentHashMap，不覆写任何方法）验证继承的 ConcurrentMap 默认方法：getOrDefault 两态、forEach 全量访问+空 map 零调用、computeIfAbsent absent 调一次/present 函数零调用（计数器）/null 结果不落、computeIfPresent absent 零调用/present 更新/null 结果删映射、compute absent(旧值 null)/present/null 删、merge absent 函数零调用直存/present (old,value)/null 删、replaceAll 全量改写 | P0 |
| 5 | 异常中继（同上方法内） | 函数抛出的**同一异常实例**透传（== 比较），映射不变；null 函数 → NPE | P0 |
| 6 | testHierarchyAndGenerics | instanceof 链（CHM/CSLM→ConcurrentMap→Map）、isAssignableFrom、上转型 Map/wildcard 操作、跨实现 equals/hashCode（Map 契约）、entrySet 活视图 | P1 |

易错规范细节（实测锁定）：
- `putIfAbsent` present 时返回**现存值**且不覆盖——不要断言返回 null。
- `replace(k,v)` 键缺席时返回 null 且**不插入**（与 put 不同）。
- ConcurrentMap 默认方法不持有全局锁，靠 `putIfAbsent`/`replace(k,o,n)` 重试保证「不回退」；单线程下与 Map 默认行为等价，差异在并发路径（多线程互踢重试），本测试用计数器证明「present 不调函数」这一可观察契约。
- merge absent 时提供的 value **不经函数**直接落表（函数零调用）。
- JDK-8 无接口静态方法、无 `Map.copyOf` 等；代码保持 Java 8 语言级（显式泛型、匿名内部类均可）。
- CHM 驱动下 null 键/值一律 NPE（由 ConcurrentHashMap 决定，接口本身不禁止，但两大标准实现都拒绝）。

优先级：P0 = 原子分支矩阵 + 默认方法行为 + 异常中继；P1 = 结构白盒 + 层次/泛型。

断言数：108（运行时 `check` 计数）。
