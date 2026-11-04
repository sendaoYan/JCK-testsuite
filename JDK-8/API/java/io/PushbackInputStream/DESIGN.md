# java.io.PushbackInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「读一次再 unread 回来」的冒烟。重点补齐构造器参数校验、
> LIFO 回退顺序、回退缓冲满（IOException）、read 参数校验、available/skip 语义、
> mark/reset 不支持、close 后 IOException。

## 1. API 提供了哪些功能

- **构造器（2）**：`(InputStream)`（回退缓冲 1 字节）、`(InputStream,int size)`（`size<=0` 抛 IAE）。
- **回退**：`unread(int)`、`unread(byte[])`、`unread(byte[],off,len)`；缓冲满抛 `IOException("Push back buffer is full")`。
- **读**：`read()` / `read(byte[],int,int)`（先消费回退区再读底层）。
- **定位/状态**：`available()`（底层 + 回退区计数）、`skip(long)`（`n<=0` 返回 0，先消费回退区）、`markSupported()==false`、`reset()` 抛 IOException。
- **生命周期**：`close()` 幂等；关闭后 read/unread 抛 `IOException("Stream closed")`。

## 2. Java 用户如何使用

- 词法扫描超前读：读一字节判定类型，不合则 `unread` 回去。
- 依赖 LIFO 顺序（多次 unread 后按后进先出吐出）；依赖默认缓冲 1 字节的约束。
- 依赖 `available()` 把已回退字节计入可读量；依赖 `skip` 先耗尽回退区。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/PushbackInputStream.html`）：构造器 size<=0 抛 IAE、unread 满抛 IOException、mark/reset 不支持。
- **JDK 实现源码**（`src/share/classes/java/io/PushbackInputStream.java`）：`unread(byte[],off,len)` 仅依赖 `System.arraycopy` 做 null/越界校验（无显式 NPE/IOBE 前置检查）、`skip(n<=0)` 返回 0、`reset` 抛 "mark/reset not supported"。

## 4. 测试用例设计（映射到 PushbackInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：默认缓冲 1、size<=0 抛 IAE | testConstructors |
| P0 | unread(int) 先出、LIFO 多次回退、EOF | testUnreadInt |
| P0 | unread(byte[])/unread(byte[],off,len) 区域回退、精确内容 | testUnreadArray |
| P0 | 缓冲满抛 IOException、失败后缓冲原样、null/越界（新鲜非满缓冲） | testBufferOverflow |
| P0 | read(byte[],off,len) 参数校验：null→NPE、off/len 非法→IOBE、len==0→0 | testReadArrayValidation |
| P1 | available 含回退计数；skip 先消费回退区、负数/0 返回 0 | testAvailableSkip |
| P1 | markSupported false、mark no-op、reset→IOException | testMarkReset |
| P1 | close 幂等、关闭后 read/unread→IOException | testClose |

## 关键规范细节（易错，已锁入断言）

- `unread(byte[],off,len)` **不显式校验** null/off/len，只在 `System.arraycopy` 时抛 NPE/IOBE；因此「缓冲已满」时先抛 IOException（满检查在前）。断言 null/越界须用**未满**的新实例。
- `skip(long)` 对 `n<=0` 返回 0（不抛异常），区别于 `PushbackReader.skip` 对负数抛 IAE。
- 默认构造器的回退缓冲大小恰为 1，第二次 `unread` 即抛 IOException。
- `reset()` 抛 `IOException("mark/reset not supported")`；`mark(int)` 为 no-op 不抛。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（继承 FilterInputStream、protected buf/pos、transferTo 存在）+ 黑盒 transferTo（JDK 9+ 方法）。

## 深化追加（契约级，批次 ioC）

新增维度清单（testUnreadExactMessages / testClosedMessages / testUnreadHighBits / testSkipPushbackInteraction / testAvailableCounting / testRereadPattern / testBulkReadSpansPushbackAndStream / testInterleavedSequence + checkThrowsMsg）：

1. **回推满精确消息**：unread(int)/unread(byte[],off,len)/unread(byte[]) 三种形式超容量均 → IOException **"Push back buffer is full"**（双侧逐字实测一致）；失败后缓冲内容原样（1,2,9 顺序读回）。
2. **close 后全操作精确消息**：close 幂等（双 close 不抛）；read/read(b,off,len)/unread(int)/unread(b,off,len)/available/skip/skip(负) 全部 → IOException **"Stream closed"**（注意与 FileInputStream 的 "Stream Closed" 大小写不同——这是 PushbackInputStream 自身的 ensureOpen 消息）。
3. **unread(int) 低 8 位截断**：unread(0x141)→0x41、unread(0)→0（与 EOF -1 可区分）、unread(-1)→255。
4. **skip×回推交互**：skip 先耗尽回推区**队头**（跳过 'x' 后读 'y'）；skip 恰耗尽回推区后接流字节；skip(10) 跨回推(3)+流(3)=6 且随后 EOF；pushback-only 流上 skip 只动缓冲。
5. **available 算术**：= 流剩余 + 回退计数；读走一个回退字节计数减一；批量回退加 len；空流空缓冲 0。
6. **超前读模式**：read→unread→重读同字节；unread(b,off,0) no-op。
7. **单次 bulk read 跨回推+流**：n=5 且回退字节 LIFO 在前（b,a,c,d,e）；len < 回退数时只填区域、余序不乱。
8. **交错序列（parser 走查）**：a,b 读出后回退 b,a 再依序读回，接 c、EOF。

负例矩阵（双侧实测）：

| 操作 | 异常 | 消息子串（双侧一致） |
|---|---|---|
| unread 超（三种形式） | IOException | Push back buffer is full |
| close 后 read/unread/available/skip | IOException | Stream closed |
| unread((byte[])null)/read(null) | NullPointerException | — |
| unread(b,off,len) 越界（缓冲未满时） | IndexOutOfBoundsException | — |
| 构造 size<=0 | IllegalArgumentException | — |

版本差异：无（行为与消息双侧逐字节一致）。javap 差异仅同步修饰：JDK-8 mark/reset/close 均 synchronized，JDK-27 仅 reset/close synchronized、mark 非同步（不影响契约断言）。transferTo 覆写仅 JDK-27 存在（JDK-8 InputStream 无该方法），见 JDK-27 DESIGN。

产出：`PushbackInputStreamTest.java` 18 → 73 断言（jtreg Passed）。
