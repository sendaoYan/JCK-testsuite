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
