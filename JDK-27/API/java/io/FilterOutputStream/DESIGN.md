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

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（15 -> 66）：与 JDK-8 侧镜像（逐字节循环、单参路由自身 3 参、两层 close、close 中 flush 失败仍关下沉、write after close、六操作失败透传、截断、out 换绑、500 字节内容、write(b,2,0) 合法）+ JDK-27 差异断言。

### 版本差异（实测锁定，写入本侧断言）
- closed 标志幂等：二次 close 无操作（flushes==1, closes==1）；失败 close 也置位，其后的 close 不再到达下沉。
- both-fail close：close 异常为主、flush 异常 suppressed（与 JDK-8 相反）。
- 帮助消息：OOB "Range [x, x + n) out of bounds for length y"；null 数组 "Cannot read the array length"；null 下沉 ""this.out" is null"（消息级断言）。
