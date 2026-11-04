# java.io.StringReader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器 null NPE、read(char[],off,len) 校验、skip 负数返回 0、mark/reset、close 后 IOException。

## 1. API 功能
- 构造器：`StringReader(String)`（null→NPE）。
- 读：`read()`（EOF -1）、`read(char[],int,int)`。
- 定位：`skip(long)`（负数 0）、`ready()`、`markSupported()`（true）、`mark(int)`（负数→IAE）、`reset()`。

## 2. 使用方式
- 把 String 包装为 Reader；mark/reset 重放；close 后读抛 IOException。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/StringReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器 null NPE | testConstructor |
| P0 | read() EOF；read(char[],off,len) 校验 + 精确 offset | testReadSingle、testReadArray |
| P1 | skip 精确/越过/负数 0 | testSkip |
| P1 | ready | testReady |
| P0 | mark/reset 重放/未标记回 0/mark(-1) IAE | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |

## 关键细节
- `skip(负数)` 返回 0（不抛，区别于 BufferedReader）；构造 null 因 `s.length()` NPE；JDK-27 侧同源 + 反射白盒。
