# java.io.CharArrayWriter 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器负容量 IAE、write(int) 低 16 位、write 校验、writeTo、append、reset/toCharArray 副本、flush/close no-op。

## 1. API 功能
- 构造器（2）：`CharArrayWriter()`、`CharArrayWriter(int)`（负→IAE）。
- 写：`write(int)`、`write(char[],int,int)`、`write(String,int,int)`（继承 `write(String)` 抛 IOException）。
- 追加：`append(CharSequence)`、`append(CharSequence,int,int)`、`append(char)`。
- 传输/查询：`writeTo(Writer)`、`reset()`、`toCharArray()`、`size()`、`toString()`、`flush()`/`close()` no-op。

## 2. 使用方式
- 收集字符到内存；`toCharArray()` 取副本；`writeTo(writer)` 直拷；`reset()` 复用。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/CharArrayWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器负容量 IAE | testConstructors |
| P0 | write(int) 低 16 位 | testWriteInt |
| P0 | write(char[],off,len) 校验 | testWriteArrayValidation |
| P0 | write(String,off,len) 内容 + null NPE + 坏索引 IOBE | testWriteString |
| P1 | writeTo 拷贝/不消耗/null NPE | testWriteTo |
| P1 | append 各重载（含 null csq→"null"） | testAppend |
| P0 | reset/size/toCharArray 副本/toString | testResetSizeToCharArray |
| P1 | flush/close no-op | testFlushClose |

## 关键细节
- 继承的 `write(String)`（单参）来自 Writer，**抛 IOException**；`write(String,off,len)` 被覆写不抛。
- JDK-27 侧同源 + 反射白盒。
