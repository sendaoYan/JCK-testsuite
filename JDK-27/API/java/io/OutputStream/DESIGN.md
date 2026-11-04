# java.io.OutputStream 测试方案 (DESIGN)

## 1. API 功能
- 抽象 `write(int)`；`write(byte[])` 委托 `write(byte[],int,int)`，后者默认逐字节循环；`flush`/`close` 默认 no-op；静态 `nullOutputStream()`。

## 2. Java 用户如何使用
- 自定义字节汇只实现 write(int) 即获得全部批量语义；装饰器（DataOutputStream 等）以此为根。

## 3. 开源代码实际用法调研
- 默认实现与检查 — `jdk25u-dev/src/java.base/share/classes/java/io/OutputStream.java`（write(byte[],int,int) 循环、null/越界检查、nullOutputStream 关闭后抛 IOException）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/OutputStream/`。

## 4. 测试用例设计 + 自动化实现（OutputStreamTest.java）
- 白盒: abstract、Closeable+Flushable、write(int) 抽象、唯一公共无参构造、nullOutputStream。
- 黑盒场景:
  1. write(byte[])/write(b,off,len) 区域语义与零长 no-op。
  2. 默认区域写逐字节调用 write(int)（计数证明）。
  3. write(null)/越界 NPE/IOOBE。
  4. 默认 flush/close no-op。
  5. 覆写的 flush/close 经基类引用被调用并计数。
  6. nullOutputStream 全静默、close 后 write 抛 "Stream closed"。
  7. ByteArrayOutputStream 经基类引用（子类语义对照）。
- 健壮性: 全内存。

产出：`OutputStreamTest.java`（白盒 10 + 黑盒 14 断言）。

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- write(byte[]) 委托 write(byte[],int,int)（Spy 探针）；JDK-27 精确消息（Range/NPE 指名 b）
- write(int) 透传 int 不截断；默认循环符号扩展（0x80 → -128）
- 默认循环顺序/区域保真；异常逐字透传已写部分保留；子类 flush/close 可抛
- try-with-resources 恰好一次 close
- nullOutputStream 全矩阵：write(int)/write(byte[])/write(b,off,len)/flush 全吞；write(null) NPE("b" is null)、write(null,0,1) NPE、write(b,0,3) IOOBE(Range)；close 幂等；关闭后 write(int)/write(byte[])/write(b,off,len) → IOException("Stream closed")；关闭后 flush 仍允许（空实现）
- 白盒：nullOutputStream 静态工厂存在、write(byte[],int,int) 具体

负例矩阵：write(null) NPE / write(b,3,0) IOOBE(Range 消息) / write(b,1,MAX) IOOBE / nullOutputStream 关闭后三种 write IOException("Stream closed") / nullOutputStream write(null) NPE。

版本差异记录：越界消息 JDK-8 null vs 27 "Range [...]"；NPE 消息 8 null vs 27 指名 "b"；nullOutputStream 仅 27 侧存在；nullOutputStream 关闭后 write 抛 "Stream closed" 而 flush 不抛（与 FileOutputStream 同型）。
