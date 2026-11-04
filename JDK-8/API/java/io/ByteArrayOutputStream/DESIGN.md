# java.io.ByteArrayOutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐构造器负容量 IAE、write 参数校验（NPE/IOBE）、
> toByteArray 副本语义、自动扩容不丢数据、reset 复用、toString(String) 的 UEE、writeTo null NPE、close no-op。

## 1. API 提供了哪些功能

- **构造器（2）**：`ByteArrayOutputStream()`（初始 32）、`ByteArrayOutputStream(int)`（负 → `IllegalArgumentException`）。
- **写**：`write(int)`（低 8 位）、`write(byte[],int,int)`（参数校验）、继承 `write(byte[])`。
- **查询/转换**：`size()`、`toByteArray()`（新副本）、`reset()`、`toString()`、`toString(String)`（未知字符集 → `UnsupportedEncodingException`）、`toString(int hibyte)`。
- **传输/生命周期**：`writeTo(OutputStream)`、`close()` no-op。

## 2. Java 用户如何使用

- 收集字节 → `toByteArray()` 取回；`writeTo(out)` 直拷到另一流；`reset()` 复用缓冲区。
- `toString("UTF-8")` 以显式字符集解码非 ASCII；依赖 `write(int)` 只保留低 8 位。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/ByteArrayOutputStream.html`）+ `src/share/classes/java/io/ByteArrayOutputStream.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/io/ByteArrayOutputStream`：`WriteBounds`（负 offset/length）、`ToString` 等。
- **实现源码**：`write(byte[],off,len)` 校验条件 `off<0 || off>b.length || len<0 || off+len-b.length>0 → IOBE`；`write(int)` 存 `(byte)b`。

## 4. 测试用例设计（映射到 ByteArrayOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：默认/0 合法、负 → IAE | testConstructors |
| P0 | write(int) 低 8 位 | testWriteInt |
| P0 | write(byte[],off,len) 校验：null→NPE、off<0/off>b.length/len<0/off+len 越界→IOBE、off==b.length&len==0 合法、write((byte[])null)→NPE | testWriteArrayValidation |
| P0 | write 精确 offset + 自动扩容不丢数据 | testWriteArrayAndGrow |
| P0 | toByteArray 每次新副本、size 不受副本修改影响 | testToByteArrayCopy |
| P1 | reset 清空并复用 | testReset |
| P1 | toString()/toString(String) 非 ASCII/未知字符集 UEE/toString(int hibyte) | testToString |
| P1 | writeTo 拷贝、不消耗源、null→NPE | testWriteTo |
| P1 | close 幂等 no-op | testCloseNoOp |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 的越界条件是 `off > b.length`（不是 `off >= b.length`），因此 `off==b.length && len==0` 合法。
- `write(int)` 只保留低 8 位（`0x1FF → 0xFF`，`256 → 0`）。
- `toString()` 使用平台默认字符集，测试只用 ASCII 字节避免跨平台差异；非 ASCII 走 `toString("UTF-8")`。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；非 ASCII 显式 UTF-8；headless 安全。
- JDK-27 侧额外保留反射白盒 + `writeBytes(byte[])`（JDK 11+）/`toString(Charset)`（JDK 10+）。
