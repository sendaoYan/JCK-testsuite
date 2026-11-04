# java.io.BufferedWriter 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 功能
- 构造器（2）：`BufferedWriter(Writer)`、`BufferedWriter(Writer,int)`（null→NPE、size<=0→IAE）。
- 写：`write(int)`、`write(char[],int,int)`、`write(String,int,int)`、`newLine()`、`flush()`。

## 2. 使用方式
- 包装 Writer 缓冲字符；`newLine()` 写平台行分隔符；flush/close 刷写。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/BufferedWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Writer、write(int)/newLine/flush/close 存在 | testWhiteBox |
| P0 | 构造器 null NPE/size IAE；write(int) 低 16 位 | testConstructors、testWriteInt |
| P0 | write(char[],off,len) 校验；write(String,off,len) | testWriteArrayValidation、testWriteString |
| P1 | newLine；flush/close | testNewLine、testFlushClose |

## 关键细节
- `write(int)` 低 16 位；null 委托构造时 NPE。

## 深化追加（契约级）

新增维度（断言 10 → 61）：与 JDK-8 侧同套（ProbeWriter 探针、size-1 逐字符下沉语义、newLine=lineSeparator、size-4 自动下沉 flushes==0、长写 pass-through、write 边界含 null-0-len 不抛、append 委托 "xABde"、flush 传播、关闭矩阵）。

负例矩阵（实测双侧一致）：null 构造 NPE；"Buffer size <= 0" IAE；关闭后 "Stream closed"；write("abc",1,5) StringIndexOOB；write((char[])null,0,1) NPE。

版本差异：无行为差异；JDK-27 NPE/IOOBE 消息更详细（helpful messages），测试按类型级断言保持两侧同源。
