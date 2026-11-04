# java.io.StringWriter 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源；注意 `write(String,off,len)` 的 null 语义与 JDK-8 差异。

## 1. API 功能
- 构造器（2）：`StringWriter()`、`StringWriter(int)`（负→IAE）。
- 写：`write(int)`、`write(char[],int,int)`、`write(String)`、`write(String,int,int)`。
- 追加：`append(CharSequence)`、`append(CharSequence,int,int)`、`append(char)`。
- 查询：`toString()`、`getBuffer()`；`flush()`/`close()` no-op。

## 2. 使用方式
- 收集字符到内存；`getBuffer()` 返回内部 StringBuffer。

## 3. 参考来源
- javadoc + `~/git/jdk/src/java.base/share/classes/java/io/StringWriter.java`。

## 4. 测试用例设计
| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 反射白盒：继承 Writer、write(int)/append/getBuffer/toString/flush 存在 | testWhiteBox |
| P0 | 构造器负容量 IAE；write(int) 低 16 位；write(char[],off,len) 校验 | testConstructors、testWriteInt、testWriteArrayValidation |
| P0 | write(String) null→"null"；write(String,off,len) 内容 + JDK 9+ null→"null" | testWriteString |
| P1 | append 各重载；getBuffer 活缓冲；flush/close no-op | testAppend、testGetBuffer、testFlushClose |

## 关键细节
- JDK 9+ `write(String,off,len)` 委托 `StringBuffer.append(null,off,len)` → 追加 "null"（JDK-8 抛 NPE）。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1。

### 新增维度
- write(int) 低 16 位边界矩阵：0/0xD7FF/0xE000/0xFFFF/0x10041(→'A')/0x1FFFF(→0xFFFF)。
- 继承 write(char[])（声明 throws IOException）：委托 write(cbuf,0,len)；(char[])null → NPE 且不写任何内容；空数组 no-op。
- write(String,off,len) 完整边界矩阵（-1,1 / 0,-1 / 1,3 / 4,0）→ IOOBE，失败调用不改动缓冲；off==length 且 len==0 合法（空区间）；代理对原样写入。
- append 协变返回 StringWriter（r1==this×3）；append(null,0,4) 写 "null"；append(csq,s,e) 越界 → IOOBE。
- getBuffer：同一活引用（两次 ==）；toString 恒与 buffer 一致；toString 结果是不可变快照（后续 write 不影响旧 String）；外部改 buffer 可见。
- 初始容量 0 合法；容量 1 增长到 100 数据完整。
- close 无操作深契约：close 后 write(int)/write(String)/write(cbuf,o,l)/append/flush/二次 close/getBuffer/toString 全部照常。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 备注 |
|---|---|---|
| new StringWriter(-1) | IllegalArgumentException | 已有 |
| write((char[])null[,..]) | NullPointerException | - |
| write((String)null) | - | 写入 "null"（非异常） |
| write((String)null,0,4) | JDK-8 NullPointerException / JDK-27 无异常写 "null" | **版本差异**（JDK-9+ 改为委托 StringBuffer.append） |
| write("abc",越界) | IndexOutOfBoundsException | JDK-8 实际类 StringIndexOutOfBoundsException，JDK-27 为普通 IndexOutOfBoundsException（均为 IOOBE 子类，断言 IOOBE 双侧安全） |
| append("abc",2,5)/(3,2) | IndexOutOfBoundsException | 双侧均 StringIndexOutOfBoundsException |

### 版本差异
- write((String)null,0,len)：8 侧 NPE / 27 侧追加 "null"（既有断言已区分，保持）。
- write(String,off,len) 越界的实际异常类：8=StringIndexOutOfBounds、27=IndexOutOfBounds（类型级断言统一 IOOBE，记录于 DESIGN 而非断言差异）。
- CharArrayWriter 无 toString(Charset)（易混淆项，本类 javap 双侧确认无此方法；ByteArrayOutputStream 才有）。断言 12 → 57。
