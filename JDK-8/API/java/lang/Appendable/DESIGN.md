# java.lang.Appendable 测试设计案（JDK-8）

> 覆盖目标：三重载路由实证 + null 子区间语义 + 精确越界消息（8/27 形式不同）+
> StringBuilder/StringBuffer/CharBuffer/StringWriter 四实现一致性。

## 1. API 提供了哪些功能

- 三个抽象方法（均 `throws IOException`）：
  - `Appendable append(CharSequence csq)`：`csq == null` 时追加四字符 `"null"`。
  - `Appendable append(CharSequence csq, int start, int end)`：等价
    `append(csq.subSequence(start, end))`；`csq == null` 时对字面量 `"null"` 取子区间。
  - `Appendable append(char c)`。
- JDK-8 实现类：`StringBuilder`、`StringBuffer`、`java.io.Writer`（含 StringWriter/PrintWriter）、
  `java.nio.CharBuffer`。

## 2. Java 用户如何使用

- 以接口类型链式追加：`Appendable out = new StringBuilder(); out.append("a").append('b').append(s, 1, 3);`
- `java.util.Formatter` 构造器接收 `Appendable` 作为输出目的地。

## 3. 参考来源

- Oracle Java SE 8 javadoc（Appendable）：null 语义、子区间等价条款、IOException 声明。
- 双侧实测（jdk8u372-b07 vs jdk-27.0.1.0.2+1）：`append("ab", -1, 1)` 的 IOOBE 消息——
  8 侧 `"start -1, end 1, s.length() 2"`、27 侧 `"Range [-1, 1) out of bounds for length 2"`；
  `append(null, 1, 3)` → `"ul"`（null 视作 `"null"` 后取区间）。

## 4. 测试用例设计 + 易错规范细节

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 三重载返回 this、顺序追加、null csq → "null"、(csq,s,e) 越界 IOOBE | testAppend/testBounds（既有） |
| P0 | Router 记录路由：csq/sub/char 三重载各自独立路由（日志全等）+ null 走 csq 重载 | testOverloadRouting |
| P0 | null 子区间：`append(null,1,3)` → "ul"、`append(null,0,4)` → "null"、`append(null,0,5)` → IOOBE 精确消息 | testNullSubrange |
| P0 | 越界消息逐字锁定（start>end / 负 start / end>len，MODERN 分支双 JDK 形式） | testExactBoundMessages |
| P1 | CharBuffer 作为 Appendable：三重载返回 this + 内容累积 | testCharBufferAsAppendable |
| P1 | StringWriter（Writer）作为 Appendable：返回 this + `"AB",1,2` → "B" | testStringWriterAsAppendable |
| P1 | StringBuffer 三重载接收者同一性 | testStringBufferReceiver |
| P1 | 空区间 (1,1) 不追加、NUL/MAX_VALUE char、builder 作实参、子区间==subSequence | testEmptyRangeAndEdges |
| P1 | IOException 同一实例穿透三重载 | testIoExceptionContract |

### 易错规范细节（已锁入断言）

- `append(csq, start, end)` 的 `csq == null` **不是**追加整个 "null"，而是对四字符 `"null"` 应用区间：
  `append(null, 1, 3)` → `"ul"`；区间越界（end 5 > 4）仍抛 IOOBE。
- Writer.append 的返回类型是 `Writer`（协变覆盖），经 `Appendable` 引用仍 == 原实例。
- 越界消息形式两代 JDK 不同（`start s, end e, s.length() n` vs `Range [s, e) out of bounds for length n`），
  用 `MODERN` 分支双锁。
- 三个 append 都声明受检 `IOException`，实现可抛任意实例且**同一实例**穿透（非包装）。
