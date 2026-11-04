# java.io.OutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。针对抽象基类的**默认实现**：write(byte[]) 与
> write(byte[],off,len) 从 write(int) 派生、flush/close 默认 no-op、
> 参数校验、子类覆写经基类引用派发、多态消费。

## 1. API 提供了哪些功能

- **抽象**：`write(int)`（唯一抽象方法）。
- **默认实现**：`write(byte[])` → `write(b,0,b.length)`；`write(byte[],int,int)` 逐字节循环 write(int)。
- **生命周期**：`flush()`、`close()` 默认 no-op（可被覆写）。
- **接口**：`Closeable`、`Flushable`。

## 2. Java 用户如何使用

- 继承 OutputStream 只实现 `write(int)` 即得到一个功能完整的字节输出流。
- 依赖零长度写不产生任何字节；依赖 null/越界被基类拒绝。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/OutputStream.html`）：各默认实现契约、flush/close no-op。
- **JDK 实现源码**（`src/share/classes/java/io/OutputStream.java`）：`write(byte[],off,len)` 逐字节循环 write(int)、`write(null)` 经 `b.length` 抛 NPE、越界经 `off/len` 检查抛 IOBE。

## 4. 测试用例设计（映射到 OutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认 write(byte[])/write(byte[],off,len) 到 write(int)、区域精确 | testWriteDelegation |
| P1 | 默认 write(byte[],off,len) 逐字节循环 write(int) | testDefaultWriteLoop |
| P0 | 参数校验：null→NPE、off/len 非法→IOBE | testArgumentChecking |
| P1 | 默认 flush/close no-op | testDefaultFlushClose |
| P1 | 子类覆写 flush/close 经基类引用派发 | testOverrideDispatch |
| P1 | 多态使用：具体 JDK 流经基类引用 | testPolymorphicUse |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 默认逐字节调用 `write(int)`（子类可覆写为更高效的 bulk 写）。
- `flush()`/`close()` 基类默认为 no-op；子类覆写后经基类引用调用仍派发到覆写实现。
- `write((byte[])null)` 与 `write(null,off,len)` 均抛 NPE；越界抛 IOBE。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒 + 黑盒 nullOutputStream（JDK 11+ 方法）。
