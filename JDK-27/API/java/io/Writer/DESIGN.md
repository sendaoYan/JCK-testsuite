# java.io.Writer 测试方案 (DESIGN)

## 1. API 功能
- 抽象 `write(char[],int,int)`/`flush`/`close`；默认 `write(int)`/`write(String)`/`write(String,off,len)`/`append(char/CharSequence/CharSequence,int,int)`（返回 this）；protected lock；静态 `nullWriter()`。

## 2. Java 用户如何使用
- 自定义字符汇实现三参 write+flush+close；append 链式调用；nullWriter 占位。

## 3. 开源代码实际用法调研
- 默认实现 — `jdk25u-dev/src/java.base/share/classes/java/io/Writer.java`（write/append 家族、nullWriter 关闭语义）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Writer/`。

## 4. 测试用例设计 + 自动化实现（WriterTest.java）
- 白盒: abstract、Appendable+Closeable+Flushable、三抽象方法、lock、nullWriter。
- 黑盒场景:
  1. 全部 write 形态汇入 write(char[],int,int)（捕获串验证）。
  2. 零长写 no-op。
  3. append 家族内容与返回 this；append(null) 写出 "null"。
  4. flush/close 经基类引用计数。
  5. write(null char[]/String) NPE、越界 IOOBE。
  6. nullWriter 静默接受、close 后抛 "Stream closed"。
  7. StringWriter 经基类引用。
- 健壮性: 全内存。

产出：`WriterTest.java`（白盒 12 + 黑盒 13 断言）。

## 深化追加（契约级）

实测环境：JDK 27.0.1（与 JDK 8u372 共有行为一致）。

### 新增维度
- 派生形态精确委托 / null 先于委托 / 抽象形态无基类区域预检 / 边界矩阵 / append null 语义 —— 与 8 侧同款（见 8 侧 DESIGN）。
- **nullWriter 深契约**：write(int)/write(String)/write(char[])/write(s,o,l)/write(空)/append 三形态/append(null) 全部静默吞掉；append 返回 nullWriter 自身（== 成立，既是 Writer 也是 Appendable 语义）；write((String)null)/write((char[])null) 仍 NPE；越界仍 IOOBE（char[] 与 String 两种形态均验）；flush 无操作；close 幂等；**closed 后 write 各形态/append/flush → IOException("Stream closed")**（逐字实测）。

### 负例矩阵（27 侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| nullWriter.write((String)null)/((char[])null) | NullPointerException | - |
| nullWriter.write("abc",0,9)/(new char[2],0,9) | IndexOutOfBoundsException | - |
| nullWriter 关闭后 write/append/flush | IOException | "Stream closed" |

### 版本差异（javap-27 逐名复核）
- JDK-27 Writer 独有：nullWriter()（JDK-8 无）。注意 Writer 无 transferTo（InputStream 才有）。断言 26 → 61。
