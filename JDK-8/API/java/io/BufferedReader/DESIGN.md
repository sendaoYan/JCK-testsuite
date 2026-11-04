# java.io.BufferedReader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器 null NPE/size IAE、read(char[],off,len) 校验、readLine 换行处理、skip 负数 IAE、mark/reset、close 后 IOException、lines() 流。

## 1. API 功能
- 构造器（2）：`BufferedReader(Reader)`、`BufferedReader(Reader,int)`（null→NPE、size<=0→IAE）。
- 读：`read()`（0..65535，EOF -1）、`read(char[],int,int)`、`readLine()`、`lines()`。
- 定位：`skip(long)`（负数→IAE）、`ready()`、`markSupported()`、`mark(int)`（负数→IAE）、`reset()`。

## 2. 使用方式
- 包装 Reader 提高字符读性能；`readLine()` 去掉 \n/\r\n；`lines()` 惰性流式读行。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/BufferedReader.java`；`Reader(Object lock)` null→NPE。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器 null NPE/size IAE | testConstructors |
| P0 | read() EOF；read(char[],off,len) 校验 + 精确 offset | testReadSingleChar、testReadArrayValidation |
| P0 | readLine LF/CRLF/尾随/EOF null | testReadLine |
| P1 | skip 精确/越过/负数 IAE；ready | testSkip、testReady |
| P0 | mark/reset 重放/未标记 IOException/mark(-1) IAE | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |
| P1 | lines() 流 | testLines |

## 关键细节
- `skip(负数)` 抛 IAE；`mark(-1)` 抛 IAE；`Reader(Object lock)` 使 null 委托在构造时 NPE（区别于 FilterInputStream 系）。
- JDK-27 侧同源 + 反射白盒。
