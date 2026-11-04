# java.io.SequenceInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐两流/Enumeration 构造、跨流边界读语义、
> bulk read 不跨流、available 只反映当前流、空枚举/空头流转、close 关闭所有流、
> 参数校验（EOF 短路先于校验）。

## 1. API 提供了哪些功能

- **构造器（2）**：`(InputStream,InputStream)`、`(Enumeration<? extends InputStream>)`。
- **读**：`read()` / `read(byte[],int,int)` 依次耗尽每个流；bulk read 单次调用**不跨**流边界。
- **状态**：`available()` 只返回**当前**流的可读量。
- **生命周期**：`close()` 关闭**所有**已到达与未到达的流。

## 2. Java 用户如何使用

- 把多个输入源串接成一个逻辑流（如多个文件的拼接）。
- 依赖 bulk read 不跨边界（每个源独立处理）；依赖 close 清理所有底层流（含未消费的）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/SequenceInputStream.html`）：构造器语义、read 顺序、close 关闭所有流。
- **JDK 实现源码**（`src/share/classes/java/io/SequenceInputStream.java`）：`read(byte[],off,len)` 在 `in==null`（EOF）时先返回 -1，**先于** null/越界校验；bulk read 只读当前流。

## 4. 测试用例设计（映射到 SequenceInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 两流构造顺序耗尽、EOF | testTwoStreamConstructor |
| P0 | bulk read 不跨流边界、EOF、参数校验（新鲜非空流） | testBulkReadBoundary |
| P1 | available 只反映当前流、切换流后更新 | testAvailable |
| P0 | Enumeration 构造多流顺序读 | testEnumerationConstructor |
| P1 | 空枚举 EOF、空头流静默跳过 | testEmptyTransitions |
| P1 | close 关闭所有流（含未到达的） | testClose |

## 关键规范细节（易错，已锁入断言）

- `read(byte[],off,len)` 在流已耗尽（`in==null`）时**先返回 -1**，此时 `read(null,...)` 不抛 NPE。因此 null/越界校验须用**未耗尽**的流。
- bulk read 单次调用**不跨**流边界：读到当前流末尾即返回，下一次调用才切到下一流。
- `close()` 会关闭所有底层流，包括从未被读到的流。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒 + 黑盒 transferTo（JDK 9+ 方法）。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（18 -> 73）。

### 新增维度
- 转换即关闭：边界 read 时当前子流先被 close（后继仍开着）；最后子流在 EOF 转换时关闭。
- 继承 skip：跨边界 skip(3)==3 后读 4；跨两流 skip(100)==4 后 EOF；skip(0)/skip(-1)==0；close 后 skip==0（双侧实测）。
- null 构造时序：ctor(null,s2) 与 ctor((Enumeration)null) 构造时 NPE；ctor(s1,null) 构造成功、首次读正常、转换 read 抛 NPE（s1 已关闭）；NPE 后 available==0、close 正常。
- close 后状态：read==-1、available==0、read(b,0,0)==-1（in==null 短路先于校验）、read(b,0,1)==-1、二次 close 无操作。
- 继承 mark/reset：markSupported false、mark 无操作、reset -> "mark/reset not supported"。
- len==0 短路：已排空（未转换）流上 read(b,0,0) 仍返回 0（len==0 先于 EOF 检查）。
- 枚举 RuntimeException：nextElement 抛出从 read() 与 close() 双路透传。
- 失败子流 close 中止链（JDK-8 实测差异）：首个 IOException 透传、无 suppressed、后继流保持未关闭。
- 失败子流 read/available 透传；read 失败不发生转换（失败子流保持当前，重试再抛）。
- 空中间流静默跨越；单元素枚举；构造急切拉首个元素（available==3）；300 字节/30 流内容一致；区域读不跨界。

### 负例精确异常表（双侧实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| read(b2,0,3)/(b,-1,1)/(b,0,-1) | IndexOutOfBoundsException | JDK-8 消息 null；JDK-27 "Range [x, x + n) out of bounds for length y" |
| read(null,0,1) | NullPointerException | 双侧 null |
| ctor((Enumeration)null) | NullPointerException | JDK-8 null；JDK-27 ""this.e" is null" |
| reset | IOException | "mark/reset not supported"（双侧一致） |
| 枚举 nextElement 抛 RuntimeException | 原样透传（read 与 close 双路） | "enum boom" |

### 版本差异（实测锁定）
- close 异常聚合：JDK-8 首个异常即中止（剩余流不关、suppressed=0）；JDK-27 继续关闭全部剩余流并聚合 suppressed。
