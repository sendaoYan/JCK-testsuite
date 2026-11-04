# java.io.StringWriter 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源；注意 `write(String,off,len)` 的 null 语义与 JDK-8 差异。

## 1. API 功能
- 构造器（2）：`StringWriter()`、`StringWriter(int)`（负→IAE）。
- 写：`write(int)`、`write(char[],int,int)`、`write(String)`、`write(String,int,int)`。
- 追加：`append(CharSequence)`、`append(CharSequence,int,int)`、`append(char)`。
- 查询：`toString()`、`getBuffer()`；`flush()`/`close()` no-op。

## 2. 使用方式
- 收集字符到内存；`getBuffer()` 返回内部 StringBuffer。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/StringWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Writer、write(int)/append/getBuffer/toString/flush 存在 | testWhiteBox |
| P0 | 构造器负容量 IAE；write(int) 低 16 位；write(char[],off,len) 校验 | testConstructors、testWriteInt、testWriteArrayValidation |
| P0 | write(String) null→"null"；write(String,off,len) 内容 + JDK 9+ null→"null" | testWriteString |
| P1 | append 各重载；getBuffer 活缓冲；flush/close no-op | testAppend、testGetBuffer、testFlushClose |

## 关键细节
- JDK 9+ `write(String,off,len)` 委托 `StringBuffer.append(null,off,len)` → 追加 "null"（JDK-8 抛 NPE）。
