# java.io.FilterOutputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 装饰基类：`protected OutputStream out`；构造 `FilterOutputStream(OutputStream)`（protected）。
- `write(int)` 透传；`write(byte[],int,int)` 逐元素调用 write(int)；`flush()`、`close()` 透传（close 前 flush）。

## 2. Java 用户如何使用
- 继承并覆盖 write 做过滤/编码/计数，PrintStream/DataOutputStream 是其子类。
- 小心：基类 bulk write 是逐字节循环（性能），子类常覆盖。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 逐字节 write 循环与 close→flush 顺序 — `jdk25u-dev/src/java.base/share/classes/java/io/FilterOutputStream.java`（`for (int i = 0; i < len; i++) out.write(b[off + i])`）。
- BufferedOutputStream 覆盖 bulk write 的对比 — 同包 BufferedOutputStream.java。

## 4. 测试用例设计 + 自动化实现（FilterOutputStreamTest.testBlackBox）
1. 包装 ByteArrayOutputStream：write(int)/write(byte[]) 内容与顺序精确一致。
2. close() 后流关闭（BAOS 容忍 close；再写不抛——验证透传语义而非强制失效）。
3. flush() 透传无异常。
4. 继承 write(byte[],int,int) 的逐字节路由：Tracked 子类统计 write(int) 次数==len。
- 健壮性: 纯内存 BAOS。

产出：`FilterOutputStreamTest.java`（白盒 + 黑盒共 15 断言）。
