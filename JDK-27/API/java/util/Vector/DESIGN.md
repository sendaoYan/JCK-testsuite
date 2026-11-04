# java.util.Vector 测试方案

对应测试：`VectorTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 构造器：`Vector()`、`Vector(int initialCapacity)`、`Vector(int, int capacityIncrement)`、
  `Vector(Collection<? extends E>)`
- List 操作：`add(E)` / `add(int, E)` / `addAll`、`get(int)` / `set(int, E)` / `remove(int)` /
  `remove(Object)`、`indexOf(Object[, int])` / `lastIndexOf(Object[, int])` / `contains` / `size` /
  `isEmpty` / `clear`、`removeAll` / `retainAll`
- 遗留方法：`addElement` / `removeElement` / `removeElementAt` / `insertElementAt` /
  `setElementAt` / `elementAt` / `firstElement` / `lastElement` / `removeAllElements` / `elements()`
- 容量：`capacity()`、`ensureCapacity(int)`、`trimToSize()`、`setSize(int)`
- 转换：`toArray()` / `toArray(T[])`、`copyInto(Object[])`、`clone()`
- 迭代：`iterator()`、`listIterator()`、`spliterator()`、`forEach`

## 2. Java 用户如何使用

```java
Vector<String> v = new Vector<>(3, 5);   // 初始容量 3，扩容增量 5
v.addElement("a"); v.elementAt(0);       // 遗留 API
v.indexOf("a"); v.indexOf("a", 1);       // 从指定下标查找
v.copyInto(array);                        // 拷入数组
Enumeration<String> e = v.elements();     // 枚举
while (e.hasMoreElements()) { e.nextElement(); }
v.setSize(n);                             // 调整大小（补 null / 截断）
```

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/io/FilePermission.java:1236` — `Vector<Permission> permissions = new Vector<>(perms.values());`（从集合构造）
- `java/net/SocketPermission.java:1457` — `Vector<SocketPermission> permissions = new Vector<>(perms.values());`
- `java/util/Vector.java`（类 javadoc）— 官方说明“与 ArrayList 同步版本；若无需线程安全，建议用 ArrayList”
- `java/util/Stack.java:50` — `public class Stack<E> extends Vector<E>;`（Vector 作为 Stack 基类）

结论：现代 JDK 源码中 Vector 使用很少，主要作为“线程安全动态数组”或遗留 Stack 基类；新代码倾向
ArrayList / ArrayDeque。

## 4. 测试用例设计 + 自动化实现

- 正常：容量增长（initialCapacity + capacityIncrement 精确值）；`add/get/set/remove`；
  `indexOf/lastIndexOf(fromIndex)`；遗留 `addElement/elementAt/insertElementAt/…`。
- 组合链路：`add/addElement → get/elementAt → indexOf/lastIndexOf → remove/removeElement →
  copyInto/elements → setSize → subList`。
- 边界/异常：`elementAt/get/set/remove(int)` 越界 → `ArrayIndexOutOfBoundsException`；
  `copyInto` 目标过小 → `ArrayIndexOutOfBoundsException`；结构修改后 `iterator/subList` →
  `ConcurrentModificationException`；`setSize` 变大填 null、变小截断。

健壮性：固定输入、无墙钟/随机；容量断言取确定性精确值（3→8）；顺序结构无迭代顺序依赖；无全局副作用；
headless，无 `@key`。
