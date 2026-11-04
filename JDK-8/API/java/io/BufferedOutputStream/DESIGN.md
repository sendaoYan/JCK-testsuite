# java.io.BufferedOutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐构造器负容量 IAE、write(int) 低 8 位与缓冲、write 参数校验、
> 大块直写路径、flush/close 语义。

## 1. API 提供了哪些功能

- **构造器（2）**：`BufferedOutputStream(OutputStream)`、`BufferedOutputStream(OutputStream,int)`（size<=0 → IAE）。
- **写**：`write(int)`（低 8 位，缓冲）、`write(byte[],int,int)`（len>=buf.length 直写）、`flush()`。

## 2. Java 用户如何使用

- 写小数据先入缓冲，flush/close 或缓冲满时刷到底层；大块数据直写避免二次拷贝。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/BufferedOutputStream.java`。
- **实现源码**：`write(byte[],off,len)` 无显式校验，null/坏索引由 `System.arraycopy` 抛 NPE/AIOOBE；`len>=buf.length` 走 `flushBuffer()+out.write` 直写。

## 4. 测试用例设计（映射到 BufferedOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：size<=0 IAE | testConstructors |
| P0 | write(int) 低 8 位 + 缓冲（flush 前底层为空） | testWriteIntAndBuffering |
| P0 | write(byte[],off,len) 校验（NPE/IOBE） | testWriteArrayValidation |
| P1 | 大块写直写底层 + 内容一致 | testLargeWrite |
| P0 | flush 清缓冲；close 隐含 flush 剩余字节 | testFlushClose |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 校验依赖底层，坏索引抛 IndexOutOfBoundsException（ArrayIndexOutOfBoundsException 子类）、null 抛 NPE。
- `len>=buf.length` 时直写底层，不经过缓冲。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
- JDK-27 侧同源 + 反射白盒。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（9 -> 60）。

### 新增维度
- 精确自动刷新边界：8 缓冲 8 次 write 后仍缓冲，第 9 次触发 8 字节下沉（1 次 bulk，0 次单字节）。
- 数组写路径矩阵：len==size 直接 bulk；len<size 缓冲；3+10 -> 先刷 3 再直写 10（2 次 bulk）；len==0 零下沉；partial-fit（5 已缓冲 + 4 数组）先刷 5 再缓冲 4。
- 默认缓冲 8192 可观测（8193 次触发）。
- flush 每次委托且空缓冲不写数据。
- close = flushBuffer + flush + close，下沉序 W1,F,C 实测锁定。
- close 非幂等（JDK-8 实测差异）：第二次 close 重新 flush + 重新 close（2/2）。
- write after close：宽容下沉可达；拒绝下沉 -> 缓冲后 flush 抛 IOException。
- write(int) 截断（-1/-256/0x100/0x141）经缓冲往返。
- 构造面：仅 2 个公共构造器，无无参构造（双侧 javap 确认）。
- 10KB/64 缓冲跨刷新内容一致；子区域写精确。

### 负例精确异常表（双侧实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| write(b,0,-1)/(b,3,1)/(b,-1,1) | ArrayIndexOutOfBoundsException | arraycopy 路径（类型断言） |
| write(null,0,1) | NullPointerException | 类型断言 |
| 构造 size<=0 | IllegalArgumentException | "Buffer size <= 0"（双侧一致） |
| close 后 flush（拒绝下沉） | IOException | 下沉自定义消息透传 |

### 版本差异（实测锁定）
- close 幂等性：JDK-8 第二次 close 重新 flush/close；JDK-27 因 FOS closed 标志为无操作。
