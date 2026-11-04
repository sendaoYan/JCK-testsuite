# java.util.Stack 测试方案

对应测试：`StackTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 构造器：`Stack()`（继承自 Vector 的默认构造）
- 栈操作：`push(E)`、`pop()`、`peek()`、`empty()`、`search(Object)`
- 继承自 Vector：`size()`、`get(int)`、`capacity()`、`iterator()` 等

## 2. Java 用户如何使用

```java
Stack<String> s = new Stack<>();
s.push("a"); s.push("b");          // 压栈（返回被压入元素）
s.peek();                            // 看栈顶（不移除）
s.search("a");                       // 距栈顶 1-based 位置，找不到 -1
s.pop();                             // LIFO 弹出
if (s.empty()) { ... }               // 判空
```

注意：`pop()/peek()` 在空栈抛 `EmptyStackException`；`search` 用 `equals` 判等、相等元素取离栈顶最近者。

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/util/Stack.java`（类 javadoc）— 官方明确建议“更完整、一致的 LIFO 栈操作用 `Deque` 接口及其
  实现（`ArrayDeque` 更优），如 `Deque<Integer> stack = new ArrayDeque<>();`”
- 现代 JDK 源码中几乎没有对 `java.util.Stack` 的直接使用；栈语义普遍由 `ArrayDeque` 或
  `LinkedList` 的 `push/pop` 承担（见 `java/nio/file/FileTreeWalker.java:61` 的 `ArrayDeque` 栈）。

结论：`java.util.Stack` 属遗留类，JDK 官方推荐以 `ArrayDeque`（Deque 接口）替代；其行为契约
（LIFO + search + EmptyStackException）仍是兼容性关键。

## 4. 测试用例设计 + 自动化实现

- 正常：`push` 返回元素、`pop` LIFO、`peek` 非破坏、`empty` 初末态、`search` 1-based 距离。
- 组合链路：`push → peek → search → pop → empty`；重复元素下 `search` 取栈顶最近者；
  继承 Vector 的迭代顺序（自底向顶）。
- 边界/异常：空栈 `pop/peek` → `EmptyStackException`；`search` 缺失 → -1。

健壮性：固定输入、无墙钟/随机；顺序结构无迭代顺序依赖；无全局副作用；headless，无 `@key`。
