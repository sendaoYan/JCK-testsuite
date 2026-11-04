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
