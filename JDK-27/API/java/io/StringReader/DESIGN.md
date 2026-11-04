# java.io.StringReader 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 功能
- 构造器：`StringReader(String)`（null→NPE）。
- 读：`read()`、`read(char[],int,int)`。
- 定位：`skip(long)`（负数 0）、`ready()`、`markSupported()`、`mark(int)`（负数→IAE）、`reset()`。

## 2. 使用方式
- 把 String 包装为 Reader；mark/reset 重放；close 后读抛 IOException。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/StringReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Reader、read/skip/mark/reset/markSupported 存在 | testWhiteBox |
| P0 | 构造器 null NPE；read EOF；read 校验 | testConstructor、testReadSingle、testReadArray |
| P1 | skip（负数 0）；ready | testSkip、testReady |
| P0 | mark/reset；close | testMarkReset、testClose |

## 关键细节
- `skip(负数)` 返回 0；null 构造因 `s.length()` NPE。
