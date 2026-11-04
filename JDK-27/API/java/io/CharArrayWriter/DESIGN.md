# java.io.CharArrayWriter 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 功能
- 构造器（2）：`CharArrayWriter()`、`CharArrayWriter(int)`（负→IAE）。
- 写：`write(int)`、`write(char[],int,int)`、`write(String,int,int)`。
- 追加/查询：`append*`、`writeTo(Writer)`、`reset()`、`toCharArray()`、`size()`、`toString()`。

## 2. 使用方式
- 收集字符到内存；`toCharArray()` 取副本；`writeTo` 直拷；`reset` 复用。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/CharArrayWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Writer、write(int)/writeTo/toCharArray/size/reset 存在 | testWhiteBox |
| P0 | 构造器负容量 IAE；write(int) 低 16 位；write 校验 | testConstructors、testWriteInt、testWriteArrayValidation |
| P0 | write(String,off,len)；writeTo；append | testWriteString、testWriteTo、testAppend |
| P0 | reset/size/toCharArray 副本/toString；flush/close no-op | testResetSizeToCharArray、testFlushClose |

## 关键细节
- 继承的 `write(String)` 抛 IOException；`write(String,off,len)` 不抛。
