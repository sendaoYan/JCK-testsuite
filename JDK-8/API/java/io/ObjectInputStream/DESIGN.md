# java.io.ObjectInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream)`（读流头）、`protected ()`。
- 对象读：`readObject`（final）、`readUnshared`、`defaultReadObject`、`readFields`→GetField、`registerValidation`。
- 原始数据（DataInput 全套）：`readBoolean..readDouble/readUTF/readFully/skipBytes/readLine/readUnsignedByte/readUnsignedShort`。
- 流/字节：`read()/read(byte[],int,int)`、`available`、`skipBytes`、`close`。
- 子类钩子（protected）：`resolveClass/resolveProxyClass/resolveObject/enableResolveObject/readStreamHeader/readClassDescriptor/readObjectOverride`。
- 注意：`getObjectInputFilter/setObjectInputFilter`（JEP 290）为 JDK 9+，本 JDK-8 用例不含。

## 2. Java 用户如何使用
- `try (ObjectInputStream ois = new ObjectInputStream(bais)) { T t = (T) ois.readObject(); }`。
- 类演化：`readObject` 内 `GetField gf = in.readFields(); v = gf.get("v", def);`。
- 校验：`in.registerValidation(this, priority)` 在整图读完后按优先级回调。
- 子类 `enableResolveObject(true)` + `resolveObject` 做对象替换（RMI 远端桩替换等）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectInputStream in = new ObjectInputStream(bais); ... in.readObject()` — `jdk8u-dev/jdk/test/java/io/Serializable/InvalidClassException/noargctor/Serialize/SubclassAcrossPackage.java:181-212` 区域。
- `resolveObject/enableResolveObject` 替换用法 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectInputStream.java`（checkResolve 实现，RMI 依赖此机制替换远端引用）。
- 反序列化加固：`readClassDescriptor` 中比对 `ObjectStreamClass.lookup(local).getSerialVersionUID()` 与流中描述符（StackOverflow "ObjectInputStream readClassDescriptor serialVersionUID check" 方案）。

## 4. 测试用例设计 + 自动化实现（ObjectInputStreamTest.java，纯黑盒）
黑盒 15 组场景:
1. 全字段往返 + transient 复位为默认值。
2. 混合流：对象→readInt→EOF（available/skipBytes/readLine 边界）。
3. GetField 类型化读取恢复值。
4. registerValidation 按 priority 高→低执行；抛 InvalidObjectException 从 readObject 传出。
5. resolveObject 默认不启用；enableResolveObject 返回先前状态；启用后替换生效。
6. OptionalDataException（length=4, eof=false）——对象读到原始块上。
7. StreamCorruptedException（坏流头）。
8. NotActiveException ×3（defaultReadObject/readFields/registerValidation）。
9. 回引用 identity；readUnshared 读回引用 → InvalidObjectException。
10. 写端中途异常 → 读端 WriteAbortedException（cause 链：自定义 IOException）。
11. 写端 NotSerializableException → 读端 WriteAbortedException（cause 为 NotSerializableException）。
12. resolveClass 钩子收到流中描述符名字。
13. 全套原始类型含 readUnsignedByte/readUnsignedShort。
14. readFully/skipBytes 在块数据上。
15. 关闭后 readObject/readInt 抛 IOException。
- 健壮性: 全内存流；线程无关；无全局 serial filter 状态。
