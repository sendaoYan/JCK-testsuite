# java.util.RandomAccess — 测试设计案

## 1. API 功能
标记接口：表示 List 支持随机访问（下标访问 O(1)）。无任何成员。

## 2. Java 用户如何使用
`list instanceof RandomAccess` 决定用下标循环还是迭代器；ArrayList 实现、LinkedList 不实现。
链路：instanceof 判断 -> 选择访问策略。

## 3. 开源代码实际用法调研
- OpenJDK `Collections.binarySearch/sort`（本地 `~/git/jdk25u-dev`）用 `instanceof RandomAccess`
  选择 indexed vs iterator 算法；`ArrayList implements RandomAccess`、`LinkedList` 不实现——
  第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/ArrayList/`.

## 4. 测试用例设计 + 自动化实现（RandomAccessTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 标记性 | 接口、无成员 |
| 实现 | ArrayList instanceof、LinkedList 不 instanceof |

共 2 组场景、约 4 个 check（`testBlackBox()`）。

## 深化批次 II（契约级追加）

- **标记矩阵**：ArrayList/Vector/Stack/Arrays.asList/emptyList/singletonList/nCopies→RandomAccess；
  LinkedList/AbstractSequentialList 子类→非 RandomAccess。
- **视图保标记**：ArrayList.subList、unmodifiableList(ArrayList)、synchronizedList(ArrayList) 保标记；
  LinkedList 的同名视图不带标记。
- **JDK-27 追加**：List.of()/of/copyOf/ofLazy 均实现 RandomAccess（ofLazy 需 @enablePreview）。
