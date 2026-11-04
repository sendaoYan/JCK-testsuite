# java.io.StringWriter 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器负容量 IAE、write(int) 低 16 位、write(char[],off,len) 校验、write(String) null→"null"、write(String,off,len) null NPE、append null csq、getBuffer 活缓冲、flush/close no-op。

## 1. API 功能
- 构造器（2）：`StringWriter()`、`StringWriter(int)`（负→IAE）。
- 写：`write(int)`（低 16 位）、`write(char[],int,int)`、`write(String)`、`write(String,int,int)`。
- 追加：`append(CharSequence)`、`append(CharSequence,int,int)`、`append(char)`。
- 查询：`toString()`、`getBuffer()`（活缓冲）；`flush()`/`close()` no-op。

## 2. 使用方式
- 收集字符到内存；`getBuffer()` 返回内部 StringBuffer（活对象）。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/StringWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器负容量 IAE | testConstructors |
| P0 | write(int) 低 16 位 | testWriteInt |
| P0 | write(char[],off,len) 校验 | testWriteArrayValidation |
| P0 | write(String) null→"null"；write(String,off,len) 内容 + null NPE | testWriteString |
| P1 | append 各重载（含 null csq→"null"） | testAppend |
| P1 | getBuffer 活缓冲 | testGetBuffer |
| P1 | flush/close no-op | testFlushClose |

## 关键细节
- JDK-8 `write(String,off,len)` 走 `str.substring(off,off+len)`，null 抛 NPE（JDK 9+ 改为 append null→"null"）；JDK-27 侧同源 + 反射白盒并断言该差异。
