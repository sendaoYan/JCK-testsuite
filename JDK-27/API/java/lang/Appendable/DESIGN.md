# java.lang.Appendable 测试方案

## 1. API 功能（按方法分组）

- 三个抽象方法（均 `throws IOException`）：
  - `Appendable append(CharSequence csq)`（`csq` 为 null 时追加 `"null"` 四个字符）。
  - `Appendable append(CharSequence csq, int start, int end)`（等价 `append(csq.subSequence(start,end))`；`csq` null 视作 `"null"`）。
  - `Appendable append(char c)`。

## 2. Java 用户如何使用

- 以接口类型接收实现：`Appendable out = new StringBuilder();`、`new StringBuffer()`、`java.io.Writer` 等。
- 组合链路：
  1. `out.append("hello").append('!')` 返回 `this` 可链式。
  2. `out.append(csq, 1, 3)` 追加子序列（等价 `csq.subSequence(1,3)`）。
  3. `out.append((CharSequence) null)` 追加 `"null"`。

## 3. 开源代码实际用法调研

来源：JDK 源码/测试（权威）。

- JDK 中 `Appendable` 由 `StringBuilder`/`StringBuffer`/`Writer`/`PrintWriter`/`StringWriter`/`Formatter`（`out` 参数）等实现，是统一的“可追加”抽象。
- `java.util.Formatter` 构造器接收 `Appendable`，把格式化结果写到任意实现；`StringBuilder.append`/`StringBuffer.append` 是主要实现路径。
- Spring 等框架多通过 `StringBuilder`（`Appendable` 实现）拼装，直接面向接口编程较少。

## 4. 测试用例设计 + 自动化实现

- 正常链路：自定义 `Recorder implements Appendable`（内嵌 StringBuilder，可配置 `fail`）→ `append(csq)`/`append(csq,1,3)`/`append(char)` 返回 `this` 且内容正确 → `append(csq) == append(csq,0,len)` → `append(null)`/`append(null,0,4)` 得到 `"null"`。
- 异常契约：`append` 三态在 `fail=true` 时抛 `IOException`（受检，验证声明）；`append(null,0,5)`（越界）抛 `IndexOutOfBoundsException`。
- 不变量：三个方法都返回 `this`；`StringBuilder`/`StringBuffer` 作为 `Appendable` 时行为一致。
- 自动化实现：`AppendableTest.testWhiteBox()`（反射：接口/抽象方法/`throws IOException`）+ `testBlackBox()`（`testAppend`、`testBounds`、`testContract`、`testIOException`、`testNullAndOtherImpls`）。
