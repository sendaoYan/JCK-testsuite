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
