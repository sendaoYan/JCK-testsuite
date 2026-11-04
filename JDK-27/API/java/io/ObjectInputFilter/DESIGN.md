# java.io.ObjectInputFilter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 核心：`checkInput(FilterInfo) -> Status`（决定 ALLOWED/REJECTED/UNDECIDED）。
- 静态组合子：`allowFilter(Predicate<Class<?>>, Status)`、`rejectFilter(Predicate, Status)`、`merge(f1, f2)`（第一个非 UNDECIDED 生效）、`rejectUndecidedClass(filter)`（把对类的 UNDECIDED 升级为 REJECTED）。
- 嵌套类型：`FilterInfo`（serialClass/arrayLength/depth/references/streamBytes 五访问器）、`Status`（UNDECIDED/ALLOWED/REJECTED）、`Config`（见 `java.io.ObjectInputFilter.Config/ConfigTest.java`）。

## 2. Java 用户如何使用
- `ois.setObjectInputFilter(ObjectInputFilter.allowFilter(c -> Allowed.class.isAssignableFrom(c), Status.REJECTED))`。
- 复合策略：`merge(全局过滤, 模块过滤)`；末尾 `rejectUndecidedClass` 封口（白名单模式）。
- lambda `filter = info -> info.depth() > 5 ? Status.REJECTED : Status.UNDECIDED` 防深度攻击。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectInputFilter.Config.createFilter("...")` 与 allowFilter/merge/rejectUndecidedClass 的官方组合用例 — `jdk25u-dev/test/jdk/java/io/Serializable/serialFilter/SerialFilterFunctionTest.java`。
- filter factory（FilterInThread 线程级过滤示例）— `jdk25u-dev/test/jdk/java/io/Serializable/serialFilter/SerialFilterFactoryTest.java`、`SerialFactoryExample.java`。
- 实现语义（null class → UNDECIDED、merge 首个非 UNDECIDED、RejectUndecidedFilter 对数组组件类型重过滤）— `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputFilter.java`（PredicateFilter/MergeFilter/RejectUndecidedFilter）。

## 4. 测试用例设计 + 自动化实现（指向 ObjectInputFilterTest.java 黑盒 `testBlackBox`）
- 白盒: 接口/枚举/嵌套类形态、checkInput 抽象、四个静态组合子存在、Config final、FilterInfo 五访问器。
- 黑盒场景:
  1. 手写 lambda 过滤器在**真实反序列化**中回调，记录 FilterInfo 的 arrayLength=-1/depth=1/refs=1（JEP 290 规范值）。
  2. allowFilter：命中→ifTrue、未命中→ifFalse、**null class → UNDECIDED**（规范行为）。
  3. rejectFilter 对偶断言。
  4. merge：第一个非 UNDECIDED 生效（含 REJECTED 优先、双方 UNDECIDED→UNDECIDED）。
  5. rejectUndecidedClass：类的 UNDECIDED→REJECTED、非 UNDECIDED 透传、无类保持 UNDECIDED。
  6. 真实接线：`!*` 过滤器使 readObject 抛 InvalidClassException("filter status: REJECTED")；allowFilter 白名单放行 Widget、拒绝 String。
  7. Status.valueOf/values 枚举行为。
- 健壮性: 只用**每流** setObjectInputFilter（不动全局 factory/serialFilter），无跨用例污染。

产出：`ObjectInputFilterTest.java`（白盒 ~16 + 黑盒 ~22 断言）。

## 深化追加（契约级）

新增维度（`testCombinatorSurface`…`testOtherStatusMatrix`，断言 31 → 77）：
- **组合器表面**：merge(ObjectInputFilter,ObjectInputFilter) public static；allowFilter(Predicate,Status)/rejectUndecidedClass 返回类型校验；接口带 @FunctionalInterface。
- **null 参数矩阵（实测消息）**：merge(null,*) NPE `filter`；allowFilter/rejectFilter null 谓词 NPE `predicate`；null otherStatus NPE `otherStatus`；rejectUndecidedClass(null) NPE `filter`。
- **merge 语义（源码+实测修正）**：
  - 第一过滤器返回 REJECTED → **短路**，第二不被调用（计数器实证）；
  - 否则两过滤器**都被咨询**，优势格 REJECTED > ALLOWED > UNDECIDED（五种组合全断言）；
  - `merge(f, null)` 返回 **f 本身**（同一性，引用相等断言）；
  - 过滤器返回 null 状态 → NPE `status`（第一）/ `other status`（第二）。
- **谓词异常**：allowFilter 谓词抛 ISE → 经 checkInput 直接调用**原样传播**；但经真实 OIS 读取时运行时异常被**映射为 REJECTED**（InvalidClassException 消息 `filter status: REJECTED`，不透传）。
- **setObjectInputFilter 生命周期**：初始 getObjectInputFilter()==null；设置后返回同一实例；二次设置 ISE `filter can not be set more than once`；读过对象后设置 ISE `filter can not be set after an object has been read`。
- **int[3] 双阶段**：恰 2 次回调（描述符阶段 len=-1，长度阶段 len=3，均 depth 1）。
- **otherStatus 矩阵**：null 类恒 UNDECIDED（不问 otherStatus，实测修正）；PredicateFilter.toString 形如 `predicate(..., ifTrue: ALLOWED, ifFalse:UNDECIDED)`（注意 ifFalse 后**无空格**，实测）。

负例矩阵：
| 操作 | 异常 | 消息子串 |
|---|---|---|
| merge(null, f) | NullPointerException | `filter` |
| allowFilter(null, s) / rejectFilter(null, s) | NullPointerException | `predicate` |
| allowFilter(p, null) / rejectFilter(p, null) | NullPointerException | `otherStatus` |
| rejectUndecidedClass(null) | NullPointerException | `filter` |
| merge 的过滤器返回 null | NullPointerException | `status` / `other status` |
| 二次 setObjectInputFilter | IllegalStateException | `filter can not be set more than once` |
| 读取后 setObjectInputFilter | IllegalStateException | `filter can not be set after an object has been read` |
| 抛运行时异常的过滤器（真实读） | InvalidClassException | `filter status: REJECTED` |

硬性约束：**绝不调用 Config.setSerialFilter / setSerialFilterFactory**（进程级全局状态）。
