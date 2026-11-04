# java.util.EnumSet 测试方案

## 1. API 功能（按方法分组）

- 工厂（静态）：`noneOf(Class)`、`allOf(Class)`、`of(E)`/`of(E,E)`/`of(E,E,E)`/`of(E,E,E,E)`/`of(E,E,E,E,E)`/`of(E,E...)`、`range(from,to)`、`copyOf(EnumSet)`、`copyOf(Collection)`、`complementOf(EnumSet)`。
- 核心操作：`add`/`remove`/`contains`/`clear`。
- 查询：`size`/`isEmpty`/`containsAll`。
- 集合运算：`addAll`/`removeAll`/`retainAll`（继承/重写自 AbstractSet）。
- 其它：`clone`、`spliterator`、`iterator`（按枚举声明序）。

## 2. Java 用户如何使用

- 构造：`EnumSet.noneOf(Color.class)` 起步；`EnumSet.allOf`/`of`/`range`/`complementOf`/`copyOf` 快速构建。
- 典型调用：`EnumSet.of(Color.RED, Color.BLUE)`；`EnumSet.range(from,to)`；`EnumSet.complementOf(set)`；集合运算。
- 真实组合链路：
  1. 工厂构造（allOf/noneOf/of/range/copyOf/complementOf），迭代按声明序。
  2. add/remove/contains（乱序插入仍声明序迭代）。
  3. addAll/removeAll/retainAll/containsAll 集合运算 + clone 独立性。
  4. 补集两次还原；of 重复元素静默去重；range 反向抛 IAE；copyOf 空集合抛 IAE。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- Guava `com.google.common.collect.Sets.immutableEnumSet(E anElement, E... otherElements)`：
  `return ImmutableEnumSet.asImmutable(EnumSet.of(anElement, otherElements));` 以及 `EnumSet.copyOf(collection)` —— Guava 直接包装 JDK EnumSet 工厂。
- Guava `com.google.common.io.Files.FileWriteMode`（`toSet`）：
  `modes.length == 0 ? EnumSet.noneOf(FileWriteMode.class) : EnumSet.copyOf(Arrays.asList(modes))` —— noneOf/copyOf 按需选择。
- JDK 测试 `test/jdk/java/util/EnumSet/EnumSetBash.java:56-60`：`EnumSet.allOf(enumClass)`/`EnumSet.noneOf(enumClass)`。
- JDK 测试 `test/jdk/java/util/EnumSet/JumboRange.java:41`：`EnumSet.range(e0, e1)`。
- JDK 测试 `test/jdk/java/util/zip/ZipFile/EndOfCenValidation.java:338`：`EnumSet.of(StandardOpenOption.CREATE_NEW, ...)`。

## 4. 测试用例设计 + 自动化实现

实现位置：`EnumSetTest.java`（`testWhiteBox()` + `testFactories()`/`testAddRemoveIteration()`/`testCollectionOpsAndClone()`/`testNullRejection()`）。

- 正常：工厂 + 声明序迭代 + add/remove/contains。
- 组合：集合运算 + complementOf 还原 + clone 独立性。
- 边界/异常：add(null) 抛 NPE；contains/remove(null) 返回 false；range 反向抛 IAE；copyOf(空集合) 抛 IAE；of 重复静默去重。
- 健壮性：确定性（固定枚举）；无全局副作用；无文件/网络；跨平台。headless-safe。

## 契约深化补充（第二轮：of 重载与 null/copyOf 类型边界/addAll 类型边界/空枚举/迭代器通道/跨类型 equals/varargs 别名/批量返回值/精确特征位/巨型枚举/深度序列化）

新增方法（只追加）：

| 测试点 | 用例方法 |
|---|---|
| of(E)~of(E,E,E,E,E) 全重载 + of(null)/of(e,null 数组)/of(e,{...,null}) NPE | testOfAritiesAndNull |
| copyOf(Collection) 类型边界：错误元素类型 CCE、含 null NPE、从 HashSet 推断、声明序结果 | testCopyOfTyping |
| addAll 类型边界（普通 List/错误类型 CCE/含 null NPE） | testAddAllTyping |
| 空枚举宇宙（noneOf/allOf 空、copyOf(空) IAE） | testEmptyEnumUniverse |
| 迭代器通道 + **EnumSet 迭代器非 fail-fast**（并发 add 不抛 CME 且不可见，双侧一致） | testIteratorChannels |
| 跨枚举类型 equals false | testCrossTypeEquals |
| of 不别名 varargs 数组 | testVarargsNotAliased |
| 批量返回值契约 + null NPE×4 | testBatchReturnValues |
| **JDK-27 精确特征位 == ORDERED\|DISTINCT\|SORTED\|SIZED\|NONNULL\|SUBSIZED（0x4155）**；getComparator()==null（自然序） | testSpliteratorExact |
| >64 常量（Jumbo/HugeEnumSet 路径）：range/声明序迭代/complementOf 双补还原 | testHugeEnumUniverse |
| 序列化声明序保持/空集/巨型枚举 round-trip | testSerializationDeep |

负例精确异常表（新锁定）：
- add(null)/of(null)/range(null,*)/allOf(null)/noneOf(null)/complementOf(null)/copyOf(null) → NPE
- contains(null)/remove(null) → false（不抛）
- copyOf(空 Collection) → IAE；copyOf/addAll 非枚举元素 → CCE；addAll 含 null → NPE
- range(from>to) → IAE

JDK-8 vs JDK-27 差异（已各自锁定）：EnumSet.spliterator() 特征位 JDK-8 为 0x4041（getComparator 抛 IllegalStateException），JDK 9+ 扩为 0x4155（含 SORTED，getComparator()==null）；JDK-27 另有 covariant `Spliterator<E> spliterator()` 声明；其余契约双侧一致。
