# javax.imageio.stream.IIOByteBuffer — DESIGN

## 1. API 功能
`IIOByteBuffer` 是「字节数组引用 + 偏移 + 长度」的可变三元组（直接 extends Object、无接口、非 Serializable/Cloneable/Comparable），供 `ImageInputStream.readBytes(IIOByteBuffer,int)` 以较少拷贝把一段字节交给调用方（一般指向 `ImageReader` 内部缓冲，应视为只读）。方法分组：
- 构造：`IIOByteBuffer(byte[] data, int offset, int length)`（唯一公开构造；data 可为 null，offset/length 不做任何校验）。
- 查询：`getData()`、`getOffset()`、`getLength()`。
- 修改：`setData(byte[])`、`setOffset(int)`、`setLength(int)`（全是纯 int/引用持有器，负值、MIN/MAX、length>data.length 均原样保存；**不存在** getNewLength/setNewLength——双侧 javap 确认全部公开方法恰为 6 个）。
- 异常契约：全类无任何 throws。

## 2. Java 用户如何使用
`IIOByteBuffer buf = new IIOByteBuffer(new byte[0], 0, 0); iis.readBytes(buf, n);` 之后用 `buf.getData()`（从 `getOffset()` 起长 `getLength()` 字节）读取数据段；也可直接构造 `(arr, 2, 4)` 再用三个 setter 改引用/窗口。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/ImageInputStreamImpl.java`：`readBytes(IIOByteBuffer,int)` 内 `len<0` 先抛 `IndexOutOfBoundsException`，再查 `buf==null` 抛 NPE，然后 `buf.setData(新分配数组); buf.setOffset(0); buf.setLength(实际读到的 len)`——EOF 处 length 为 -1。
- `jdk/test/javax/imageio/stream/ReadBytesIIOByteBuffer.java`：`new IIOByteBuffer(b, 0, b.length)` 后 `readBytes(iiob, -1)` 验证负长度抛 `IndexOutOfBoundsException`。

## 4. 测试用例设计 + 易错规范细节（`IIOByteBufferTest.testWhiteBox/testBlackBox`）
- 白盒：类形状（public 具体、Object 父、零接口、非 Serializable/Cloneable/Comparable）；唯一构造器签名 (byte[],int,int)；公开方法面恰为 {getData,setData,getOffset,setOffset,getLength,setLength} 六个（集合相等断言）；每个方法 public 实例、零 throws；明确断言不存在 NewLength 族方法。
- 值语义：构造三元组逐项往返；0/负/MIN/MAX 的 offset 与 length 原样保存；`setData(null)`/构造 null data 合法；length 可大于 data.length（不做交叉校验）；两个实例互不影响。
- 与 `readBytes` 集成（MemoryCacheImageInputStream 载体）：offset 恒重置 0；每次调用换新数组（旧引用失效）；连续调用继续消费流；EOF 处 length=-1 且仍返回所请求大小的数组。
- 易错点：本类是持有器不是缓冲区——负值不抛异常（用户侧自行保证窗口有效）；readBytes 之后原 data 引用被替换，别再持有旧数组下标。
