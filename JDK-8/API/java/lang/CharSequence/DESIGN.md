# java.lang.CharSequence 测试设计案（JDK-8）

> 覆盖目标：接口契约级——四抽象方法 + 默认 chars()/codePoints() 的路由实证 +
> String/StringBuilder/StringBuffer/CharBuffer 多态一致性 + 越界异常契约。

## 1. API 提供了哪些功能

- 抽象方法：`length()`、`charAt(int)`、`subSequence(int,int)`、`toString()`。
- 默认方法（JDK-8 即有）：`chars()`、`codePoints()`（IntStream 视图）。
- JDK-8 **没有**的成员：`isEmpty()`（15+ 默认方法）、静态 `compare`（11+，实为 9+）、
  `getChars`（21+）——本侧不测（javap 确认）。
- 实现类：`String`、`StringBuilder`、`StringBuffer`、`java.nio.CharBuffer` 等。

## 2. Java 用户如何使用

- 以接口类型接收多种实现：`CharSequence cs = "hello";` 或自定义实现；
  `StringBuilder.append(CharSequence)`/`String.contains(CharSequence)` 等大量 API 以此为参数。
- `cs.subSequence(1, 4)` 切窗；`cs.chars()` 流式处理字符。

## 3. 参考来源

- Oracle Java SE 8 javadoc（CharSequence）：charAt 越界抛 `IndexOutOfBoundsException` 的接口契约、
  chars()/codePoints() 为 1.8 加入的默认方法。
- OpenJDK 8u 源码：默认 chars()/codePoints() 基于索引区间逐 `charAt` 生成。
- 双侧实测（jdk8u372-b07 vs jdk-27.0.1.0.2+1）：toArray 终结操作下 charAt 调用次数 == length()（两侧一致）；
  count() 在 27 上可优化为不经 charAt（故只用 toArray 断路由）。

## 4. 测试用例设计 + 易错规范细节

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | String 经接口的四方法 + subSequence(0,len)==this | testStringAsCharSequence（既有） |
| P0 | charAt/subSequence 越界（SIOOBE ⊂ IOOBE，接口契约） | testCharAtBounds/testSubSequenceBounds（既有）+ testImplBounds |
| P0 | 自定义最小实现 Seq 的四方法精确 + 链式 subSequence 相对索引 | testCustomImplContract |
| P0 | 五实现（String/SB/SBF/CharBuffer/Seq）多态遍历：length/charAt/toString/subSequence/charAt 循环 | testPolymorphicImpls |
| P1 | 默认 chars()/codePoints() 经 charAt 路由（CountingSeq 计数 == length）+ 代理对单元/解码 | testDefaultStreamRouting |
| P1 | CharBuffer 视图：charAt 不动 position、length 计 UTF-16 单元 | testCharBufferView |
| P1 | 各窗口 (0,0)/(0,1)/(5,6)/(0,len) 边界 | testSubSequenceWindows |

### 易错规范细节（已锁入断言）

- 接口文档只承诺 `IndexOutOfBoundsException`：String/StringBuilder 实际抛其子类 SIOOBE，
  自定义实现委托 String 同样如此——断言类型用 IOOBE 上界。
- **默认 chars()/codePoints() 逐索引调 charAt**：`toArray()` 终结后 charAt 次数 == length()
  （两侧实测一致）；`count()` 在 JDK-27 可被短路优化（0 次调用），不能用来断路由。
- 代理对上 `chars()` 给两个 UTF-16 单元（0xD834/0xDD1E），`codePoints()` 解码成 1 个码点；
  `length()` 计 UTF-16 单元数。
- `subSequence(0, len)`：String 返回 **this**（== 断言），StringBuilder 实际返回 String（不断言类型）。
- CharBuffer 的 charAt 不改变 buffer position。
- JDK-8 无 isEmpty/compare/getChars：本侧不写（javap 确认后跳过）。
