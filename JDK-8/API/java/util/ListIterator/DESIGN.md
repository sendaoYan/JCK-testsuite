# java.util.ListIterator 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。nextIndex/previousIndex 计数、双向遍历边界 NSEE、
> set/add/remove 作用在最后返回元素上、以及无最后返回元素（next/previous 前、add 后、
> remove 后）时抛 IllegalStateException。

## 1. API 提供了哪些功能

- **导航**：`hasNext/hasPrevious`、`next/previous`、`nextIndex/previousIndex`。
- **写操作**：`set`（替换最后返回元素）、`add`（光标前插入）、`remove`（移除最后返回元素）。

## 2. Java 用户如何使用

- `listIterator()` / `listIterator(index)` 定位；`next()` 后 `set/remove`；
  `add` 在光标前插入，随后 `previous()` 可回到新插入元素。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/ListIterator.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/ArrayList.java`（ListItr）。

## 4. 测试用例设计（映射到 ListIteratorTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 索引计数 | testIndexes |
| P0 | 双向遍历边界 NSEE | testBidirectionalBounds |
| P0 | set/add/remove | testSetAddRemove |
| P0 | 无最后返回元素 ISE | testIllegalState |
| P0 | listIterator(index) 定位 | testListIteratorIndex |

## 关键规范细节（易错，已锁入断言）

- `next`/`previous` 越界抛 **NoSuchElementException**。
- `set/remove` 在无最后返回元素时抛 **IllegalStateException**：包括 next/previous 前、
  `add` 之后、`remove` 之后（连续 remove）。

## 注意事项

- 用 ArrayList（有序、可变）为载体；`add` 后 `set/remove` 均 ISE（lastRet 被清空）。

## 深化批次 II（契约级追加）

- **交替遍历**：next/previous 交替返回同一元素；完全走回起点后 hasPrevious=false。
- **add 可见性矩阵**：add 后 nextIndex+1；新元素对 next() 不可见、对 previous() 可见；
  其后 set 替换该新元素；头部/尾部 add。
- **set/remove 状态机**：set 立即可见；remove 后游标左移；previous 后 remove 删刚返回元素；
  set(null)/set 回填。
- **forEachRemaining**：从游标起访问剩余并耗尽，之后 previous 仍可用。
- **构造越界**：listIterator(-1)/(size+1) IOOBE；listIterator(size) 的 previousIndex=size-1。
- **CME**：结构修改后 next 和 previous 均抛；自身 add 后 remove→ISE（先 previous 再 remove 合法）。
