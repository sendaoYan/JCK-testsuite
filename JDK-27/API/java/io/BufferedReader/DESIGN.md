# java.io.BufferedReader 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 功能
- 构造器（2）：`BufferedReader(Reader)`、`BufferedReader(Reader,int)`（null→NPE、size<=0→IAE）。
- 读：`read()`、`read(char[],int,int)`、`readLine()`、`lines()`。
- 定位：`skip(long)`（负数→IAE）、`ready()`、`markSupported()`、`mark(int)`（负数→IAE）、`reset()`。

## 2. 使用方式
- 包装 Reader 提高字符读性能；`readLine()` 去换行；`lines()` 惰性流。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/BufferedReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Reader、read/readLine/skip/mark/reset/lines 存在 | testWhiteBox |
| P0 | 构造器 null NPE/size IAE；read() EOF；read 校验 | testConstructors、testReadSingleChar、testReadArrayValidation |
| P0 | readLine；skip（负数 IAE）；ready | testReadLine、testSkip、testReady |
| P0 | mark/reset；close；lines() | testMarkReset、testClose、testLines |

## 关键细节
- `skip(负数)`/`mark(-1)` 抛 IAE；null 委托构造时 NPE。

## 深化追加（契约级）

新增维度（断言 22 → 95）：与 JDK-8 侧同套（readLine 行界矩阵、混读位置、跨缓冲长行/CRLF 跨 refill、mark/reset 全契约、关闭矩阵 7 操作 + lines() UncheckedIOException cause、read 越界含 int 溢出、lines 计数）。

负例矩阵（实测，与 JDK-8 一致的精确子串）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| mark 越限后 reset | IOException | Mark invalid |
| 未标记 reset | IOException | Stream not marked |
| mark(-1) | IllegalArgumentException | Read-ahead limit |
| 关闭后 7 操作 | IOException | Stream closed |
| 关闭后 lines() 消费 | UncheckedIOException | cause 含 Stream closed |

版本差异：JDK-27 NPE 带 helpful 具名参数消息（如 read(null)），测试只断言类型级；行为契约（行界/mark/关闭）两版完全一致。
