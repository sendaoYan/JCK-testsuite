# java.io.ByteArrayOutputStream 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源，另覆盖 JDK 11+
> `writeBytes(byte[])`、JDK 10+ `toString(Charset)`。

## 1. API 提供了哪些功能

- **构造器（2）**：`ByteArrayOutputStream()`、`ByteArrayOutputStream(int)`（负 → IAE）。
- **写**：`write(int)`、`write(byte[],int,int)`、`writeBytes(byte[])`（JDK 11+）、继承 `write(byte[])`。
- **查询/转换**：`size()`、`toByteArray()`、`reset()`、`toString()`、`toString(String)`、`toString(Charset)`（JDK 10+）。
- **传输/生命周期**：`writeTo(OutputStream)`、`close()` no-op。

## 2. Java 用户如何使用

- 收集字节 → `toByteArray()`/`toString(Charset)` 取回；`writeBytes` 快速追加整数组；`reset()` 复用；`writeTo` 直拷。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** + `~/git/jdk/src/java.base/share/classes/java/io/ByteArrayOutputStream.java`。

## 4. 测试用例设计（映射到 ByteArrayOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 反射白盒：继承 OutputStream、toByteArray/size/reset/toString(Charset)/writeBytes/writeTo 存在 | testWhiteBox |
| P0 | 构造器：默认/0/负 IAE | testConstructors |
| P0 | write(int) 低 8 位；write(byte[],off,len) 校验（NPE/IOBE） | testWriteInt、testWriteArrayValidation |
| P0 | write 偏移 + writeBytes + 自动扩容不丢数据 | testWriteArrayAndGrow |
| P0 | toByteArray 副本语义；reset 复用 | testToByteArrayCopy、testReset |
| P1 | toString()/toString(String)/toString(Charset)/UEE | testToString |
| P1 | writeTo 拷贝/null NPE；close 幂等 no-op | testWriteTo、testCloseNoOp |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 越界条件是 `off > b.length`，故 `off==b.length && len==0` 合法。
- `write(int)` 只保留低 8 位；`toString()` 用平台默认字符集，ASCII 之外显式指定 UTF-8。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；非 ASCII 显式 UTF-8；headless 安全。
