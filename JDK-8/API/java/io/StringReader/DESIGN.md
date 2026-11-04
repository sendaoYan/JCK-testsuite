# java.io.StringReader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。补齐构造器 null NPE、read(char[],off,len) 校验、skip 负数返回 0、mark/reset、close 后 IOException。

## 1. API 功能
- 构造器：`StringReader(String)`（null→NPE）。
- 读：`read()`（EOF -1）、`read(char[],int,int)`。
- 定位：`skip(long)`（负数 0）、`ready()`、`markSupported()`（true）、`mark(int)`（负数→IAE）、`reset()`。

## 2. 使用方式
- 把 String 包装为 Reader；mark/reset 重放；close 后读抛 IOException。

## 3. 参考来源
- javadoc + `src/share/classes/java/io/StringReader.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器 null NPE | testConstructor |
| P0 | read() EOF；read(char[],off,len) 校验 + 精确 offset | testReadSingle、testReadArray |
| P1 | skip 精确/越过/负数 0 | testSkip |
| P1 | ready | testReady |
| P0 | mark/reset 重放/未标记回 0/mark(-1) IAE | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |

## 关键细节
- `skip(负数)` 返回 0（不抛，区别于 BufferedReader）；构造 null 因 `s.length()` NPE；JDK-27 侧同源 + 反射白盒。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（双侧逐项探针验证，行为完全一致）。

### 新增维度
- 代理对（supplementary）：read() 依次返回高/低代理项（0xD83D/0xDE00），read(char[],off,len) 原样透传；BMP 边界 \u0000/\uD7FF/\uE000/\uFFFF 全部存活。
- bulk read 边界：请求多于剩余 → 返回剩余；len==0 在 EOF 处仍返回 0；off==cbuf.length 且 len==0 合法；off+len int 溢出 → IOOBE；精确适配读取。
- skip 极值：skip(0)==0、skip(Long.MAX_VALUE)==剩余、EOF 处 skip==0、skip(Long.MIN_VALUE)==0（不抛）。
- ready()：只要流未关闭恒为 true —— 空串、EOF 后均 true（与 CharArrayReader 的 EOF false 相反，属文档化差异）。
- mark 深契约：readAheadLimit 不被强制（mark(0) 后越过 limit 再 reset 有效）；EOF 处 mark 后 reset 重放；最近一次 mark 生效；mark/reset 可落在代理对两半之间。
- close 矩阵：close 后 read/read(cbuf,off,len)/skip(±)/ready/mark/reset/read(CharBuffer) 全部 IOException("Stream closed")；**关闭检查先于越界检查**（read(new char[2],5,1) 报 closed 而非 IOOBE）；但基类 read(char[]) 的 null 长度检查先于委托 → read((char[])null) 是 NPE。
- 继承 read(CharBuffer)：填满 remaining、EOF -1、零 remaining → 委托一次 len==0 → 0、只读缓冲 → ReadOnlyBufferException、null → NPE。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| new StringReader(null) | NullPointerException | - |
| read(null,0,1)/read((char[])null) | NullPointerException | - |
| read(cbuf,越界/负/off+len 溢出) | IndexOutOfBoundsException | - |
| mark(-1) | IllegalArgumentException | - |
| close 后任一操作 | IOException | "Stream closed"（逐字） |

### 版本差异
- 无行为差异；JDK-27 仅多反射白盒（concrete/extends Reader 等结构项）。断言 19 → 76。
