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
