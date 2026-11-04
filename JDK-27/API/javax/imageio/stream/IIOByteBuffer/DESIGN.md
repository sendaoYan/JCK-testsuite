# javax.imageio.stream.IIOByteBuffer — DESIGN

## 1. API 功能
`IIOByteBuffer` 是一个「字节数组引用 + 偏移 + 长度」的可变三元组，供 `ImageInputStream.readBytes(IIOByteBuffer,int)` 以较少拷贝的方式把一段字节交给调用方（一般指向 `ImageReader` 内部缓冲，应视为只读）。方法分组：
- 构造：`IIOByteBuffer(byte[] data, int offset, int length)`。
- 查询：`getData()`、`getOffset()`、`getLength()`。
- 修改：`setData(byte[])`、`setOffset(int)`、`setLength(int)`。

## 2. Java 用户如何使用
`IIOByteBuffer buf = new IIOByteBuffer(new byte[0], 0, 0); iis.readBytes(buf, n);` 之后用 `buf.getData()/getOffset()/getLength()` 读取数据段；也可直接 `new IIOByteBuffer(arr, 2, 4)` 再 `getData()/getOffset()/getLength()` 或调用三个 setter 改引用。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/ImageInputStreamImpl.java`：`readBytes(IIOByteBuffer,int)` 内 `buf.setData(data); buf.setOffset(0); buf.setLength(len)`。
- `test/jdk/javax/imageio/stream/ReadBytesIIOByteBuffer.java`：`new IIOByteBuffer(b, 0, b.length)` 后调用 `iis.readBytes(iiob, -1)` 验证负长度抛 `IndexOutOfBoundsException`。

## 4. 测试用例设计 + 自动化实现（`IIOByteBufferTest.testBlackBox`）
- 构造 `(data,2,4)` 断言 `getData()==data/getOffset()==2/getLength()==4`。
- `setData/setOffset/setLength` 后断言往返一致。
- 边界：`setOffset(-5)`、`setLength(0)` 作为纯 int 持有器被原样保留。

## 5. 深化补充（第二轮：契约级覆盖）
追加 `testStructure`（类形状：Object 父、零接口、非 Serializable/Cloneable/Comparable；唯一构造器 (byte[],int,int)；公开方法面恰为 6 个 get/set 对集合相等断言；明确断言**不存在** getNewLength/setNewLength——任务书中提及但双侧 javap 确认无此方法族）与 `testValues`：
- 值语义：0/负/MIN/MAX 的 offset 与 length 原样保存（纯持有器，无校验）；构造 null data 与 setData(null) 合法；length 可大于 data.length；实例间独立。
- 与 `readBytes` 集成（MemoryCacheImageInputStream）：offset 恒重置 0、每次调用换新数组、连续调用继续消费流、EOF 处 length=-1 且仍返回请求大小的数组（javadoc 未写，实测行为）。
