# java.io.FilterOutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。装饰器基类：write(int)/write(byte[])/
> write(byte[],off,len) 委托底层；close 隐含 flush；flush 委托；null/越界校验；
> 子类覆写拦截；protected `out` 访问；底层异常透传；null 委托在首次写抛 NPE。

## 1. API 提供了哪些功能

- **构造器**：`public FilterOutputStream(OutputStream out)`。
- **写**：`write(int)` 委托 `out.write(b)`；`write(byte[],off,len)` 逐字节调用 `write(int)`（JDK 8 不调用底层 bulk 写）。
- **生命周期**：`flush()` 委托；`close()` 先 `flush()` 再 `out.close()`。
- **字段**：`protected OutputStream out`（子类可访问）。
- **语义**：构造器**不**校验 null 委托；首次写才抛 NPE。

## 2. Java 用户如何使用

- 作为自定义输出装饰器（日志、大小写转换）的基类，覆写 `write(int)`。
- 依赖 protected `out` 字段直接访问被包装流。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/FilterOutputStream.html`）：委托契约、close 隐含 flush。
- **JDK 实现源码**（`src/share/classes/java/io/FilterOutputStream.java`）：`write(byte[],off,len)` 用位运算校验越界后**逐字节**调用 write(int)；`close()` 先 flush 再 close。

## 4. 测试用例设计（映射到 FilterOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | write(int) 委托 | testSingleWrite |
| P0 | write(byte[])/write(byte[],off,len) 经 write(int) 区域精确 | testBulkWrite |
| P1 | flush 委托 | testFlush |
| P1 | close 先 flush 后 close | testClose |
| P0 | 参数校验：null→NPE、越界→IOBE | testArgumentChecking |
| P1 | 子类覆写拦截（大小写转换） | testSubclassInterception |
| P1 | protected `out` 字段访问 | testProtectedOut |
| P1 | 底层异常透传（消息保留） | testFailingSink |
| P1 | null 委托在首次写抛 NPE | testNullDelegate |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 逐字节调用 `write(int)`，**不**调用底层 `write(byte[],off,len)`。
- 构造器**不**校验 null 委托，首次写才抛 NPE。
- `close()` 隐含一次 `flush()`，再 `close()`。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（public 构造器、protected out 字段）。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（8 -> 62）。

### 新增维度
- write(b,off,len) 逐字节循环 write(int)（singles=n, bulk=0，双侧实测）；write(byte[0]) 零下沉；中途失败前 2 字节已交付。
- write(byte[]) 路由到自身 3 参（子类 bulk 覆写胜出：bulk=1, singles=0）。
- close = flush + close 精确一次；二次 close 重新执行（JDK-8 无 closed 标志，实测 2/2）。
- 两层 close：下沉 flush=2/close=1；二次 4/2（JDK-8）。
- close 中 flush 失败：异常透传但下沉仍被 close（closes=1）；失败后的 close 重试整链（3/2）。
- both-fail close：flush 异常为主、close 异常 suppressed（JDK-8 try-with-resources 语义，实测与 JDK-27 相反）。
- write after close 宽容下沉可达；六操作失败下沉 "sink broken" 透传；null 下沉五操作 NPE；write(int) 截断；out 中途换下沉；500 字节循环内容一致。
- OOB：裸 IndexOutOfBoundsException（JDK-8 位运算校验，消息 null）；write(b,2,0) 合法、write(b,3,0) 拒绝。

### 负例精确异常表（双侧实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| write(b,3,1)/(b,0,-1)/(b,1,2)/(b,3,0) | IndexOutOfBoundsException | JDK-8 消息 null；JDK-27 "Range [x, x + n) out of bounds for length y" |
| write(null,0,1)/write((byte[])null) | NullPointerException | JDK-8 null；JDK-27 "Cannot read the array length" |
| null 下沉五操作 | NullPointerException | JDK-8 null；JDK-27 ""this.out" is null" |
| both-fail close | IOException | 主 F + suppressed C（JDK-8）/ 主 C + suppressed F（JDK-27） |

### 版本差异（实测锁定）
- 二次 close：JDK-8 重执行（2/2）；JDK-27 无操作（1/1）。
- 异常消息：JDK-27 全部带帮助文本；JDK-8 均为 null。
