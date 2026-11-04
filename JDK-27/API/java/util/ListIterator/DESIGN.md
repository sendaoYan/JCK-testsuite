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
