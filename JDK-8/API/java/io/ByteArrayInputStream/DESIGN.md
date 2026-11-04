# java.io.ByteArrayInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「方法被调用一次」的冒烟。重点补齐异常/参数校验、null 语义、
> EOF（-1）与零字节读的歧义、skip 边界、mark/reset 初始 mark、close 幂等 no-op。

## 1. API 提供了哪些功能

- **构造器（2）**：`ByteArrayInputStream(byte[])`、`ByteArrayInputStream(byte[],int,int)`。
  - 缓冲数组**不拷贝**；`count = min(offset+length, buf.length)`（越界被钳制，不抛异常）。
  - `(byte[])` 的 `pos=0,count=buf.length`；`(byte[],off,len)` 的 `pos=off,mark=off`。
- **读**：`read()`（0..255 无符号，EOF 返回 -1）、`read(byte[],int,int)`（参数校验 + 精确 offset 放置）。
- **定位/状态**：`skip(long)`（负数返回 0）、`available()`（`count-pos`）、`markSupported()`/`mark(int)`/`reset()`。
- **生命周期**：`close()` 为 no-op（关闭后仍可读、可 reset）。

## 2. Java 用户如何使用

- `new ByteArrayInputStream(bytes)` 把内存字节包装为 `InputStream`，供需要流接口的 API 消费。
- `read(b, off, len)` 精确把数据放进目标数组指定偏移；依赖 `read()` 的 0..255 无符号语义（0xFF→255）。
- 依赖 `mark/reset` 重放一段数据；依赖 `skip(n)` 快速越过前缀（负数当作 0）。
- 依赖 `close()` 后流仍可用（内存流无资源）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/ByteArrayInputStream.html`）：构造器不拷贝、`count=min(offset+length,buf.length)`、close no-op。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/io/ByteArrayInputStream`：无专类回归，行为直接对齐 `src/share/classes/java/io/ByteArrayInputStream.java`。
- **JDK 实现源码**（`ByteArrayInputStream.java`）：`read(byte[],off,len)` 的校验顺序（null→NPE；off<0/len<0/len>len-off→IOBE；`pos>=count` 先于 `len<=0` 短路返回 -1）、`skip` 负数返回 0、`mark=pos`。

## 4. 测试用例设计（映射到 ByteArrayInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：缓冲区不拷贝、offset/length 读取、off+len 钳制到 buf.length、length==0 空流、offset==buf.length 空流、null→NPE | testConstructors |
| P0 | read() 无符号 0..255、EOF 反复 -1、空流 -1 | testReadSingleByte |
| P0 | read(byte[],off,len) 参数校验：null→NPE、off<0/len<0/len>剩余→IOBE | testReadArrayValidation |
| P0 | read 精确 offset 放置、零长度读（非 EOF 返 0 / EOF 返 -1）、末尾部分读返回剩余 | testReadArrayContract |
| P1 | skip：精确返回、越过末尾返回剩余、负数/0 不动位置 | testSkip |
| P1 | available：初始/读后/EOF | testAvailable |
| P0 | mark/reset：markSupported、未 mark reset 回 0、mark 后重放、mark(0)、offset 构造器初始 mark=offset | testMarkReset |
| P1 | close 幂等 no-op：关闭后 available/read/skip/reset 仍可用 | testCloseNoOp |

## 关键规范细节（易错，已锁入断言）

- **构造器不校验边界**：`(byte[],off,len)` 用 `Math.min(offset+length, buf.length)` 钳制，负 offset 属未定义行为，**不写负 offset→IOBE 的断言**（JDK 实现并不抛）。
- `read(byte[],off,len)` 在 `pos>=count` 时**先于** `len<=0` 判断返回 -1，因此「EOF 上零长度读」返回 -1 而非 0；「非 EOF 上零长度读」返回 0。
- `skip(n)` 对负数返回 0 且不动位置；对超过剩余字节数返回实际剩余。
- 初始 `mark`：默认构造为 0，offset 构造为 offset。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（继承 InputStream、readAllBytes/readNBytes/transferTo 存在）+ 黑盒 readAllBytes/transferTo/readNBytes（JDK 9+ 方法）。

## 深化追加（契约级）

本轮（批次 ioA）在原有结构覆盖上追加契约级断言（43 -> 68）。

### 新增维度
- 构造边界：off 越过 buf.length（available() 返回负数、read() 返回 -1，不抛异常——len 被钳制到 0）；负 offset 在 read 时才抛 AIOOBE。
- read 边界：off == b.length 且 len == 0 合法返回 0；len 钳制到剩余量（len 超出只返回剩余）。
- skip × mark 交互：skip 移动 pos 但不动 mark；mark 后 skip 再 reset 回放。
- close 后 mark/reset/read 全部正常（close 是无操作，对象状态不变）。
- 大缓冲（100KB）遍历 + 内容一致性。

### 负例精确异常表（双侧实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| new BAIS(buf, -1, 2) 后 read() | ArrayIndexOutOfBoundsException | 双侧消息为 null（JVM 抛出） |
| read(b, -1, 1) / read(b, 0, -1) / read(b, 0, len+b.length) | IndexOutOfBoundsException（JDK-8 为 AIOOBE） | null |

### 版本差异（已 javap/实测锁定）
- JDK-27 新增 readAllBytes/readNBytes(byte[],int,int)/readNBytes(int)/transferTo，JDK-8 无；本侧文件不得引用。
