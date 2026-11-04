# SerialBlob 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialBlob` 是 `java.sql.Blob` 的离线可序列化实现，把 BLOB 字节内容物化到内存 `byte[]`，供断开的 RowSet 使用（JDK 源码 `src/java.sql.rowset/share/classes/javax/sql/rowset/serial/SerialBlob.java`）。

- 构造器：`SerialBlob(byte[])`、`SerialBlob(Blob)`（Blob 须先把数据取回客户端）。
- 读取：`length()`、`getBytes(long pos, int length)`（1-based）、`getBinaryStream()`、`getBinaryStream(long pos, long length)`。
- 搜索：`position(byte[] pattern, long start)`、`position(Blob pattern, long start)`，找不到或 start 越界返回 -1。
- 写入：`setBytes(long pos, byte[])`、`setBytes(long pos, byte[], int offset, int length)`、`setBinaryStream(long pos)`（仅 Blob 构造支持）。
- 修改：`truncate(long)`。
- 生命周期/对象语义：`free()`（后续调用为 no-op，free 后再访问抛 `SerialException`）、`equals`、`hashCode`、`clone`。

## 2. Java 用户如何使用
```java
SerialBlob b = new SerialBlob(new byte[]{1,2,3,4,5});
long n = b.length();                 // 5
byte[] head = b.getBytes(1, 2);      // {1,2}（1-based）
long p = b.position(new byte[]{3,4}, 1); // 3
b.setBytes(6, new byte[]{7,8});      // 追加，长度变 7
b.truncate(3);                       // 截断
SerialBlob copy = (SerialBlob) b.clone();
b.free();                            // 之后 length/getBytes 抛 SerialException
```
组合链路：`new(byte[]) → length → getBytes → position → setBytes → truncate → clone/equals → free`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialBlobTests.java`：覆盖 `getBinaryStream(pos,len)`、`setBytes(pos,bytes)`、`setBytes(pos,bytes,offset,length)`、`truncate(0/3)`、`position(byte[],start)`、free 后各方法抛 `SerialException`、`clone` 等，是行为断言的主要来源。
- JDK 实现 `SerialBlob.java`：确认 `getBytes` 1-based、`position` 找不到返回 -1、`free` 置 `buf=null`、`clone` 拷贝内部字节数组并置 `blob=null`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialBlobTest.testBlackBox`）覆盖：
- 主链路：构造→length→getBytes(1-based 切片/全量/超长截断)→getBinaryStream(全量/区间)→position(byte[]/Blob/未找到/越界)→setBytes(覆盖/追加/offset+length/越界异常)→truncate(0/3/越界异常)→空数组→equals/hashCode/clone→free 后异常。
- 边界：`getBytes` 越界 pos 抛 SerialException；`position` 找不到/start 越界返回 -1；`setBytes` pos=0 抛 SerialException；`truncate` 超长抛 SerialException。
- 异常路径：free 后 `length`/`getBytes` 抛 SerialException。
- 确定性：全部内存数据、固定字节序列，无时间/网络/随机依赖；headless，不加 `@key`。
