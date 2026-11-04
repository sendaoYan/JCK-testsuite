# java.util.List — 测试设计案

## 1. API 功能
有序可重复集合基接口。继承 SequencedCollection。JDK-27 预览新增 `ofLazy`。

- 抽象：按位置 get/set/add/remove、indexOf/lastIndexOf、listIterator、subList、size。
- default：sort/replaceAll/spliterator/getFirst/getLast/addFirst/addLast/removeFirst/removeLast。
- static：of/copyOf（以及预览 ofLazy）。

## 2. Java 用户如何使用
`List.of("a","b")` 不可变工厂、`ArrayList` 可变操作、`sort`/`replaceAll` 就地、`subList` 视图、
`ofLazy(size, gen)` 惰性生成。链路：of -> get/set/add -> sort/replaceAll -> subList -> getFirst/getLast。

## 3. 开源代码实际用法调研
- OpenJDK `List`（本地 `~/git/jdk25u-dev`）：of 拒绝 null、copyOf 浅拷贝、ofLazy 按索引惰性求值——
  第 1/2/4 节对齐；JDK-27 ofLazy 需 `--enable-preview`。
- 官方测试 `test/jdk/java/util/List/`.

## 4. 测试用例设计 + 自动化实现（ListTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| of 工厂 | 空/多元素、null 抛 NPE |
| copyOf | 拷贝内容 |
| 首末操作 | getFirst/getLast/addFirst/removeFirst |
| 就地操作 | sort/replaceAll/subList |
| ofLazy | 惰性生成、负尺寸抛 IAE |

共 5 组场景、约 14 个 check（`testBlackBox()`）。

## 深化批次 II（契约级追加）

- **hashCode/equals 契约**：公式锁定（1/128/4066/126046）跨 ArrayList/LinkedList/Vector 一致；
  equals 元素序敏感、大小敏感、null 元素相等性。
- **sort 默认方法**：nullsFirst、comparing 键提取、reversed 链、**稳定性**（相等键保序）。
- **replaceAll 默认方法**：Arrays.asList set 透传可用；unmodifiableList→UOE；null 元素映射。
- **Arrays.asList 视图**：set 返回旧值透传；add/remove/clear→UOE；subList add→UOE 而 set 透传。
- **subList 视图契约**：equals/hashCode 按自身内容；set/add/clear 透传；全范围 subList equals 原表。
- **越界矩阵**：get/set/add/remove/addAll/listIterator 全部 (-1)/(size+1) IOOBE 且失败不留痕。
- **JDK-27 追加**：List.of 全矩阵（迭代序=参数序、hashCode 公式、全部写通道 UOE、null NPE、
  RandomAccess、subList 不可变、空表查询/越界）；List.copyOf（快照独立、不可变、null NPE、subList 源）；
  **List.ofLazy**（size() 不调生成函数、get 每下标恰一次并缓存、迭代、不可变、null fn NPE、负 size IAE、
  RandomAccess）；排序稳定性与比较器链。
