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

## 深化追加（契约级）

新增维度（全部经 JDK-8 实测锁定）：
- write(byte[]) → write(byte[],int,int) 委托证明（Spy 探针）；零长数组仍走委托但不写
- 参数矩阵：write(b,3,0)/write(b,1,MAX) → IOOBE；write(b,b.length,0) 合法边界；write(empty) no-op；JDK-8 IOOBE/NPE 消息为 null
- write(int) 原样透传 int（不截断）——截断到低 8 位是子类契约；默认循环对 byte 符号扩展后传给 write(int)（0x80 → -128），子类需自行 & 0xFF
- 默认循环顺序/区域保真：off/len 区域两端与内部、零长跳过循环
- 异常传播：循环中 write(int) 的 IOException 逐字透传，已写部分保留
- 子类 flush/close 允许抛 IOException
- try-with-resources 恰好一次 close
- 白盒：write(byte[],int,int) 在 JDK-8 即具体方法（循环实现，非 abstract）；JDK-8 无 nullOutputStream 工厂（反射断言）；抽象类拒绝实例化（InstantiationException）

负例矩阵：write(null) NPE / write(null,0,1) NPE / write(b,2,1) IOOBE / write(b,-1,1) IOOBE / write(b,0,-1) IOOBE / write(b,3,0) IOOBE。

版本差异（8 vs 27）：JDK-27 越界消息 "Range [...]"、NPE 指名 "b"（JDK-8 均为 null）；JDK-27 另有 nullOutputStream（8 侧无，见 27 侧 DESIGN）。
