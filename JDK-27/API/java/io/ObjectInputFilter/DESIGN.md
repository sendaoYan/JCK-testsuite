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
