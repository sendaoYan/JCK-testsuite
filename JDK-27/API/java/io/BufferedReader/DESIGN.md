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
