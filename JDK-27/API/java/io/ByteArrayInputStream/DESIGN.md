# java.io.ByteArrayInputStream 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源，另覆盖 JDK 9+
> `readAllBytes`/`readNBytes`/`transferTo`。

## 1. API 提供了哪些功能

- **构造器（2）**：`ByteArrayInputStream(byte[])`、`ByteArrayInputStream(byte[],int,int)`。
  - 缓冲数组**不拷贝**；`count = min(offset+length, buf.length)`（越界被钳制，不抛异常）。
- **读**：`read()`（0..255 无符号，EOF -1）、`read(byte[],int,int)`、JDK 9+ `readAllBytes()`/`readNBytes(byte[],int,int)`/`transferTo(OutputStream)`。
- **定位/状态**：`skip(long)`、`available()`、`markSupported()`/`mark(int)`/`reset()`。
- **生命周期**：`close()` no-op。

## 2. Java 用户如何使用

- 把内存字节包装为 `InputStream`；`readAllBytes()` 一次性取回；`transferTo(out)` 直拷贝；
  `readNBytes` 精确读入指定偏移；依赖 `read(b,off,len)` 的无符号/EOF 语义。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** + `~/git/jdk/src/java.base/share/classes/java/io/ByteArrayInputStream.java`。
- **OpenJDK 回归** `~/git/jdk/test/jdk/java/io/ByteArrayInputStream`（无专类，行为对齐源码）。

## 4. 测试用例设计（映射到 ByteArrayInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 反射白盒：继承 InputStream、readAllBytes/readNBytes/transferTo/markSupported 存在 | testWhiteBox |
| P0 | 构造器：不拷贝、offset/length、钳制、空流、null→NPE | testConstructors |
| P0 | read() 无符号/EOF；read 参数校验（NPE/IOBE）；read 精确 offset/零长度歧义/末尾部分读 | testReadSingleByte、testReadArrayValidation、testReadArrayContract |
| P1 | skip 边界（负数/越过末尾）；available；mark/reset（未 mark、重放、offset 初始 mark） | testSkip、testAvailable、testMarkReset |
| P1 | close 幂等 no-op | testCloseNoOp |
| P1 | JDK 9+ readAllBytes/transferTo/readNBytes 往返 + 末尾部分读 | testJdk9Plus |

## 关键规范细节（易错，已锁入断言）

- **构造器不校验边界**：`count=min(offset+length,buf.length)`，负 offset 未定义，不写负 offset→IOBE。
- `read(byte[],off,len)` 在 `pos>=count` 时先于 `len<=0` 返回 -1（EOF 上零长度读=-1，非 EOF 零长度读=0）。
- `skip(n)` 负数返回 0；初始 `mark`（默认 0，offset 构造为 offset）。

## 注意事项

- 固定输入，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。

## 深化追加（契约级）

本轮（批次 ioA）在原有结构覆盖上追加契约级断言（50 -> 109）：JDK-8 侧全部镜像维度（构造边界、read 边界钳制、skip×mark、close 无操作、100KB 大缓冲）加上 JDK-27 独有方法契约。

### 新增维度（JDK-27 独有）
- readAllBytes：空流返回空数组（非 null）；pos 前进到 EOF；mark 跨 readAllBytes 存活；只传剩余部分。
- readNBytes(byte[],off,len)：精确放置；流尾不足返回实际数；EOF 处 len==0 返回 0；参数校验抛 NPE/IOOBE（不做 read 式钳制——readNBytes 用 Objects 校验，read 才钳制）。
- readNBytes(int)：返回实际读到的字节数组。
- transferTo：计数/内容/EOF/剩余；null -> NPE；下沉抛 IOException 透传。

### 负例精确异常表（实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| readNBytes(b,0,10) 且 b.length<10 | IndexOutOfBoundsException | "out of bounds for length" |
| transferTo(null) | NullPointerException | 双侧类型断言（消息来自 Objects.requireNonNull） |

### 版本差异
- readAllBytes/readNBytes×2/transferTo 仅 JDK-9+ 存在；JDK-8 侧文件不引用。
