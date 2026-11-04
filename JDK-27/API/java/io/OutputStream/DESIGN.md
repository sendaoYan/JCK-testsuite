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
