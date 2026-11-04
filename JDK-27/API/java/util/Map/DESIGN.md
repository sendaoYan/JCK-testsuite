# java.util.Map — 测试设计案

## 1. API 功能
键值对集合基接口。JDK-27 预览新增 `ofLazy`。

- static：of/ofEntries/copyOf/entry（以及预览 ofLazy）。
- default：getOrDefault/forEach/replaceAll/putIfAbsent/remove(k,v)/replace/compute*/merge。

## 2. Java 用户如何使用
`Map.of("a",1)` 不可变工厂、`entry` 构造条目、`getOrDefault`/`computeIfAbsent`/`merge` 缺省操作、
`ofLazy(keys, fn)` 惰性求值。链路：of -> get/put -> default 操作 -> entry/copyOf。

## 3. 开源代码实际用法调研
- OpenJDK `Map`（本地 `~/git/jdk25u-dev`）：of 拒绝 null 键值、重复键抛 IAE、entry.setValue 抛 UOE——
  第 1/2/4 节对齐；JDK-27 ofLazy 需 `--enable-preview`。
- 官方测试 `test/jdk/java/util/Map/`.

## 4. 测试用例设计 + 自动化实现（MapTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| of 工厂 | 空/多元素、重复键 IAE、null 键值 NPE |
| entry | getKey/getValue、setValue 抛 UOE |
| copyOf | 拷贝内容 |
| default 操作 | getOrDefault/computeIfAbsent/merge/putIfAbsent |
| ofLazy | 惰性求值 |

共 5 组场景、约 14 个 check（`testBlackBox()`）。


## 5. 契约级深化追加（2026-11，38→191 断言）

新增方法：testImmutableFactories（JDK-9+ 工厂）/ testEqualsHashCodeFormula / testEntryContract /
testDefaultMethodImplementationPaths / testPresentNullValueBranches / testViewSemantics /
testIterationAndInvariants（与 JDK-8 侧同源，另有 27 侧白盒 testWhiteBox/ofLazy 保留）。

| 维度 | 锁定内容 |
|---|---|
| Map.of 全家 | of() ~ of(k,v,...)、ofEntries、entry、copyOf；全部 mutator（put/putIfAbsent/remove/clear/compute/merge/replaceAll）UOE；null key/value NPE；重复键 IAE；copyOf(null) NPE |
| of 迭代序 | **不保证参数序**（哈希探针布局，实测 Map.of("a",1,"b",2,"c",3) 序为 [c,b,a]）——只断言内容集合相等，不断言顺序（与 List.of 不同！） |
| equals/hashCode 公式 | {"a"=1,"b"=2,"c"=3} hash == 288（96×3）；与 LinkedHashMap 同内容 equals；Map.entry("a",1).hashCode()==96、与 SimpleEntry 互等 |
| copyOf 独立性 | 原 HashMap 后续修改不传导到 copy；copy 含 null value → NPE |
| present-null-value 分支 / 默认方法路径 / 视图 | 同 JDK-8 侧（ProbeMap 探针 + 全分支矩阵） |
