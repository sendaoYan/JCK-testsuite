# java.io.Reader 测试方案 (DESIGN)

## 1. API 功能
- 抽象 `read(char[],int,int)`/`close`；默认 `read()`/`read(char[])`/`read(CharBuffer)`；`skip`/`ready`/`mark`/`reset`/`markSupported`；`readAllLines`/`readAllAsString`/`transferTo(Writer)`；静态 `nullReader()` 与 `Reader.of(CharSequence)`；protected lock。

## 2. Java 用户如何使用
- 自定义字符源实现三参 read+close；`Reader.of(cs)` 把 CharSequence 变只读 Reader；nullReader 占位。

## 3. 开源代码实际用法调研
- 默认实现 — `jdk25u-dev/src/java.base/share/classes/java/io/Reader.java`（read()/read(char[])/read(CharBuffer)/skip/transferTo/readAllLines）。
- Reader.of 与 nullReader — 同文件静态工厂。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Reader/`。

## 4. 测试用例设计 + 自动化实现（ReaderTest.java）
- 白盒: abstract、Readable+Closeable、两抽象方法、lock、两个静态工厂。
- 黑盒场景（最小子类）:
  1. read()/read(char[]) 派生与 EOF。
  2. read(CharBuffer) 计数与内容。
  3. skip 正向、skip(-1) IAE。
  4. markSupported false、reset/mark 抛 "reset()/mark() not supported"、ready false。
  5. readAllLines/readAllAsString/transferTo。
  6. Reader.of 反映后续 CharSequence 修改（活视图）。
  7. nullReader 永空。
  8. StringReader 经基类引用。
- 健壮性: 全内存。

产出：`ReaderTest.java`（白盒 15 + 黑盒 29 断言）。

## 深化追加（契约级）

实测环境：JDK 27.0.1（与 JDK 8u372 的共有行为逐项对照，默认实现语义一致）。

### 新增维度（计数探针子类逐调用验证）
- 派生形态精确委托、默认 skip 循环（"10,7," len 序列）、skip(负) IAE "skip value is negative"、mark()/reset() 精确消息、read(CharBuffer) 默认实现细节 —— 与 8 侧同款（见 8 侧 DESIGN）。
- **nullReader 深契约**：read(char[])/read(CharBuffer) → -1；ready false；markSupported false；skip(0)==0；**skip(-5)==0 不抛 IAE**（自实现，非基类默认 —— 与基类契约相反，已显式断言）；readAllLines 空、transferTo 0；read(null CharBuffer) NPE；close 幂等；closed read/ready/skip/readAllAsString → IOException("Stream closed")。
- **Reader.of 深契约**：of(null) NPE；markSupported true、mark/reset 重放；ready true；活 CharSequence 视图（StringBuilder 构造后 append 可见）；of("") 恒 EOF；close 后 read → "Stream closed"。
- **readAllLines 行终止符矩阵**：\r\n、\r、\n 均切行；无尾终止符仍产出末行；单一尾终止符不产生空末行；"a\n\n" → ["a",""]；空输入 → 空 List；对最小子类走默认实现同样可用。
- **readAllAsString**：逐字保真（含 \r\n 与代理对）；默认实现路径验证。
- **transferTo 边界**：null → NPE；空源 → 0；目标 Writer 失败 → IOException 原消息传播；transferTo(nullWriter) 返回计数并排空源。

### 负例矩阵（27 侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| skip(-1)（默认实现） | IllegalArgumentException | "skip value is negative" |
| nullReader().skip(-5) | 无异常 | 返回 0（与基类默认相反） |
| mark/reset（默认） | IOException | "mark() not supported"/"reset() not supported" |
| Reader.of(null) | NullPointerException | - |
| transferTo(null) | NullPointerException | - |
| nullReader/of 关闭后操作 | IOException | "Stream closed" |

### 版本差异（javap-27 逐名复核）
- JDK-27 Reader 独有：nullReader()、of(CharSequence)、readAllLines()、readAllAsString()、transferTo(Writer)（JDK-8 均无，8 侧文件绝不引用）。断言 39 → 105。
