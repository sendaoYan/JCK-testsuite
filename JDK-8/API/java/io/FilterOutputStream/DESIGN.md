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
