# java.io.CharArrayReader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器 null NPE/边界 IAE（区别于 ByteArrayInputStream 不校验）、read 校验、skip 负数 0、ready EOF、mark/reset、close。

## 1. API 功能
- 构造器（2）：`CharArrayReader(char[])`、`CharArrayReader(char[],int,int)`（负 offset/offset>len/负 length/溢出 → IllegalArgumentException）。
- 读：`read()`（EOF -1）、`read(char[],int,int)`。
- 定位：`skip(long)`（负数 0）、`ready()`（EOF false）、`markSupported()`、`mark(int)`、`reset()`。

## 2. 使用方式
- 把 char[] 包装为 Reader；mark/reset 重放；offset 构造只读子区间。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/CharArrayReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器：null NPE、offset/length 钳制、负/溢出 IAE、空流 | testConstructors |
| P0 | read() EOF；read(char[],off,len) 校验 + 精确 offset | testReadSingle、testReadArray |
| P1 | skip 精确/越过/负数 0 | testSkip |
| P1 | ready 有数据/EOF | testReady |
| P0 | mark/reset 重放/未标记回 offset/offset 构造初始 mark | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |

## 关键细节
- `CharArrayReader(char[],off,len)` 校验负值抛 **IllegalArgumentException**（不是 IndexOutOfBoundsException）；`offset+length` 溢出（<0）也 IAE；合法越界钳制。
- JDK-27 侧同源 + 反射白盒。
