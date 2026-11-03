# java.util.SequencedMap — 测试设计案

## 1. API 功能
有明确迭代顺序的 Map 接口（JDK 21+）。继承 Map。

- 抽象：reversed()。
- default：firstEntry/lastEntry/pollFirstEntry/pollLastEntry/putFirst/putLast。

## 2. Java 用户如何使用
`SequencedMap<K,V> m = new LinkedHashMap<>();` 首尾插入/摘取条目、reversed() 逆序视图。
链路：putFirst/putLast -> firstEntry/lastEntry -> pollFirstEntry/pollLastEntry -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `SequencedMap`（本地 `~/git/jdk25u-dev`）default 方法委托 entrySet 迭代器；
  `LinkedHashMap` 实现——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SequencedMap/`.

## 4. 测试用例设计 + 自动化实现（SequencedMapTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 首尾插入 | putFirst/putLast 顺序 |
| 首末条目 | firstEntry/lastEntry |
| 摘取 | pollFirstEntry/pollLastEntry 删除并返回 |
| reversed | 逆序视图 |

共 4 组场景、约 10 个 check（`testBlackBox()`）。
