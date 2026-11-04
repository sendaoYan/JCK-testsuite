# java.io.BufferedWriter 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器 null NPE/size IAE、write(int) 低 16 位、write(char[],off,len) 校验、write(String,off,len)、newLine、flush/close。

## 1. API 功能
- 构造器（2）：`BufferedWriter(Writer)`、`BufferedWriter(Writer,int)`（null→NPE、size<=0→IAE）。
- 写：`write(int)`（低 16 位）、`write(char[],int,int)`、`write(String,int,int)`、`newLine()`、`flush()`。

## 2. 使用方式
- 包装 Writer 缓冲字符；`newLine()` 写平台行分隔符；flush/close 刷写。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/BufferedWriter.java`；`Writer(Object lock)` null→NPE。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器 null NPE/size IAE | testConstructors |
| P0 | write(int) 低 16 位 | testWriteInt |
| P0 | write(char[],off,len) 校验 | testWriteArrayValidation |
| P0 | write(String,off,len) 内容 + null NPE | testWriteString |
| P1 | newLine 平台行分隔符 | testNewLine |
| P0 | flush/close 刷写剩余 | testFlushClose |

## 关键细节
- `write(int)` 低 16 位；`write(String)` 走 `out.write`，null 委托在构造时 NPE。
- JDK-27 侧同源 + 反射白盒。

## 深化追加（契约级）

新增维度（断言 8 → 55）：
- ProbeWriter 探针（static nested，记录下沉字符/flush/close 计数）直接观测缓冲语义。
- size-1 缓冲语义：写第 N 个字符时下沉第 N-1 个（空→"a"→"ab"→flush "abc" 四断言实测，非想当然的"每写必下沉"）。
- newLine 精确写入 System.lineSeparator() 的字符。
- size-4 边界：第 4 字符驻留、第 5 个字符触发下沉且 flushes==0（缓冲满自动下沉 ≠ flush()）。
- 长 write(String) 超过缓冲直达底层（pass-through 单次调用）。
- write 边界：write((String)null,0,0) 不抛；write("abc",2,-1) 不抛（len 校验短路）；write("abc",1,5) → StringIndexOOB；write((char[])null,0,1) → NPE。
- append 委托链 "xABde"（append(csq,start,end) 即 write(csq,start,end)）。
- flush 向底层传播；关闭矩阵（write(int)/write(String)/write(char[])/flush/write 区域）全 "Stream closed"。

负例矩阵（实测双侧一致）：构造 null → NPE；size<=0 → IllegalArgumentException "Buffer size <= 0"；关闭后写/flush → IOException "Stream closed"；区域越界 StringIndexOOB / NPE。
