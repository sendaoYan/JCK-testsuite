# java.util.RandomAccess 测试设计案（JDK-8）

> 覆盖目标：完整契约。见下深化批次 II 小节与 RandomAccessTest.java。

## 1. API 功能 / 2. 使用方式 / 3. 参考来源

- marker 接口（RandomAccess）：标记 `get(i)` 为常数时间的 List 实现，无成员。
- 参考来源：Oracle javadoc；OpenJDK 源码实现类清单。

## 深化批次 II（契约级追加）

- **标记矩阵**：ArrayList/Vector/Stack/Arrays.asList/emptyList/singletonList/nCopies→RandomAccess；
  LinkedList/AbstractSequentialList 子类→非 RandomAccess。
- **视图保标记**：ArrayList.subList、unmodifiableList(ArrayList)、synchronizedList(ArrayList) 保标记；
  LinkedList 的同名视图不带标记。
- **JDK-27 追加**：List.of()/of/copyOf/ofLazy 均实现 RandomAccess（ofLazy 需 @enablePreview）。
