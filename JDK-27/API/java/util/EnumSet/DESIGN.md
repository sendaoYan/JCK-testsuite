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
