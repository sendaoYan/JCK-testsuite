# java.io.CharArrayReader 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 功能
- 构造器（2）：`CharArrayReader(char[])`、`CharArrayReader(char[],int,int)`（负/溢出 → IAE）。
- 读：`read()`、`read(char[],int,int)`；定位：`skip`、`ready`、`mark`、`reset`。

## 2. 使用方式
- 把 char[] 包装为 Reader；mark/reset 重放；offset 子区间。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/CharArrayReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Reader、read/skip/mark/reset/markSupported 存在 | testWhiteBox |
| P0 | 构造器 null NPE/边界 IAE/钳制；read EOF；read 校验 | testConstructors、testReadSingle、testReadArray |
| P1 | skip；ready；mark/reset；close | testSkip、testReady、testMarkReset、testClose |

## 关键细节
- 构造器负值抛 IAE（非 IOBE）；JDK-8 同。
