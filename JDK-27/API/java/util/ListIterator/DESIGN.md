# java.util.ListIterator — 测试设计案

## 1. API 功能
`Iterator` 的双向扩展：可前进/后退、按位置操作。

- 继承抽象：hasNext/next。
- 新增抽象：hasPrevious/previous/nextIndex/previousIndex/set/add。
- 继承 remove。

## 2. Java 用户如何使用
`ListIterator<E> it = list.listIterator();` 前进 next、后退 previous、nextIndex/previousIndex
定位、set 改值、add 插入、remove 删除。
链路：next 前进 -> previous 后退 -> nextIndex/previousIndex -> set/add/remove。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/ListIterator.java`（本地 `~/git/jdk25u-dev`）：
  nextIndex/previousIndex 分别 = 游标位置/游标-1；set/add/remove 作用在最后返回元素/游标处——
  第 1/3/4/5 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/ArrayList.java` 的 Itr/ListItr 是标准实现。
- OpenJDK `test/jdk/java/util/List/ListDefaults.java` 等。

## 4. 测试用例设计 + 自动化实现（ListIteratorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 索引记账 | nextIndex/previousIndex 随 next/previous 变化 |
| 双向遍历 | 前进到端、从端 previous、next 越界抛 NSEE |
| set | 替换最后返回元素 |
| add | 游标前插入、previous 返回插入元素 |
| remove | 删除最后返回元素 |
| listIterator(index) | 从指定位置开始 |

共 6 组场景、约 16 个 check（`testBlackBox()`）。

## 深化批次 II（契约级追加）

- **交替遍历**：next/previous 交替返回同一元素；完全走回起点后 hasPrevious=false。
- **add 可见性矩阵**：add 后 nextIndex+1；新元素对 next() 不可见、对 previous() 可见；
  其后 set 替换该新元素；头部/尾部 add。
- **set/remove 状态机**：set 立即可见；remove 后游标左移；previous 后 remove 删刚返回元素；
  set(null)/set 回填。
- **forEachRemaining**：从游标起访问剩余并耗尽，之后 previous 仍可用。
- **构造越界**：listIterator(-1)/(size+1) IOOBE；listIterator(size) 的 previousIndex=size-1。
- **CME**：结构修改后 next 和 previous 均抛；自身 add 后 remove→ISE（先 previous 再 remove 合法）。
