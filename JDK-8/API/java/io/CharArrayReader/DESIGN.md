# java.io.CharArrayReader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器 null NPE/边界 IAE（区别于 ByteArrayInputStream 不校验）、read 校验、skip 负数 0、ready EOF、mark/reset、close。

## 1. API 功能
- 构造器（2）：`CharArrayReader(char[])`、`CharArrayReader(char[],int,int)`（负 offset/offset>len/负 length/溢出 → IllegalArgumentException）。
- 读：`read()`（EOF -1）、`read(char[],int,int)`。
- 定位：`skip(long)`（负数 0）、`ready()`（EOF false）、`markSupported()`、`mark(int)`、`reset()`。

## 2. 使用方式
- 把 char[] 包装为 Reader；mark/reset 重放；offset 构造只读子区间。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/CharArrayReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器：null NPE、offset/length 钳制、负/溢出 IAE、空流 | testConstructors |
| P0 | read() EOF；read(char[],off,len) 校验 + 精确 offset | testReadSingle、testReadArray |
| P1 | skip 精确/越过/负数 0 | testSkip |
| P1 | ready 有数据/EOF | testReady |
| P0 | mark/reset 重放/未标记回 offset/offset 构造初始 mark | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |

## 关键细节
- `CharArrayReader(char[],off,len)` 校验负值抛 **IllegalArgumentException**（不是 IndexOutOfBoundsException）；`offset+length` 溢出（<0）也 IAE；合法越界钳制。
- JDK-27 侧同源 + 反射白盒。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（行为一致）。

### 新增维度
- **源数组别名（不拷贝）**：构造后修改源 char[] 可见（文档化 buf is not copied）；window 构造器同样别名。
- ready 深契约：EOF 后 false、空数组 false、skip 越过后 false（对照 StringReader 恒 true）。
- bulk read 边界：请求多于剩余 → 返回剩余；len==0 在 EOF 处返回 0；off==length+len==0 合法；off+len 溢出 IOOBE；read((char[])null) NPE（基类长度检查先行）；精确适配。
- skip 极值：skip(0)/skip(Long.MAX_VALUE)==剩余/EOF 处 0/skip(Long.MIN_VALUE)==0；window 构造器把 skip 限制在 [off, off+len)。
- mark 深契约：readAheadLimit 被忽略（mark(0) 越限后 reset 有效）；skip 越过 mark 后 reset 回退；EOF 处 mark 后 reset 重放；window 构造器未标记 reset 回 window 起点；首读前 mark 可全量重放。
- close 矩阵：read/read(c,o,l)/skip(±)/ready/mark/reset 全部 IOException("Stream closed")；关闭检查先于越界检查；read((char[])null) 仍 NPE。
- read(CharBuffer)：填充 remaining、EOF -1、空 reader → -1、零 remaining → 0（JDK-27 侧 CharArrayReader 覆写此方法、JDK-8 侧走 Reader 默认，可观察行为一致）。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| 构造 null / off 或 len 负 / off>buf.length | NPE / IllegalArgumentException | 已有 |
| read(null,0,1)/read((char[])null) | NullPointerException | - |
| read(cbuf,越界/溢出) | IndexOutOfBoundsException | - |
| close 后任一操作 | IOException | "Stream closed"（逐字） |

### 版本差异
- 无行为差异。断言 23 → 75。
