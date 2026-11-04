# java.io.InterruptedIOException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；公共可变字段 `int bytesTransferred`。

## 2. Java 用户如何使用
- 阻塞读被中断时捕获；读取 bytesTransferred 记录已传输字节数。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/InterruptedIOException.java`；`PipedInputStream.read` 中断抛出。

## 4. 测试用例设计 + 自动化实现（InterruptedIOExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息/默认 bytesTransferred==0。
2. bytesTransferred 可写（文档化部分传输）。
3. 中断阻塞在空 PipedInputStream.read 的线程 → InterruptedIOException，bytesTransferred==0，中断状态被消耗。
4. 普通读不受影响（对比）。
5. 作为 IOException 捕获。
- 健壮性: daemon 线程 + join 超时；无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 40，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `InterruptedIOException`：双侧同值 `4020568460727500567L`
### 新增测试方法
1. testThrowableProtocolAndField（bytesTransferred 公有可变字段）
2. testSerializationContract（bytesTransferred=512 随往返保留）
3. testDeterministicTrigger（单线程：预置中断标志 + PipedInputStream 空读 → InterruptedIOException bytesTransferred=0 且标志被消费；有数据时正常读出对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 触发实例 message 为 null（无 errno 装饰）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
