# java.io.ObjectInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream)`（读流头）、`protected ()`。
- 对象读：`readObject`（final）、`readUnshared`、`defaultReadObject`、`readFields`→GetField、`registerValidation`。
- 原始数据（DataInput 全套）：`readBoolean..readDouble/readUTF/readFully/skipBytes/readLine/readUnsignedByte/readUnsignedShort`。
- 流/字节：`read()/read(byte[],int,int)`、`available`、`skipBytes`、`close`。
- 过滤：`getObjectInputFilter`/`setObjectInputFilter`（均 final）。
- 子类钩子（protected）：`resolveClass/resolveProxyClass/resolveObject/enableResolveObject/readStreamHeader/readClassDescriptor/readObjectOverride`。

## 2. Java 用户如何使用
- `try (ObjectInputStream ois = new ObjectInputStream(bais)) { T t = (T) ois.readObject(); }`。
- 类演化：`readObject` 内 `GetField gf = in.readFields(); v = gf.get("v", def);`。
- 校验：`in.registerValidation(this, priority)` 在整图读完后按优先级回调。
- 安全（JEP 290）：`ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter("com.app.*;!*"))`。
- 子类 `enableResolveObject(true)` + `resolveObject` 做对象替换（RMI 远端桩替换等）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectInputStream in = new ObjectInputStream(bais); ... in.readObject()` — `jdk25u-dev/test/jdk/java/io/Serializable/InvalidClassException/noargctor/Serialize/SubclassAcrossPackage.java:181-212` 区域。
- `ois.setObjectInputFilter(...)` / `Config.createFilter` — `jdk25u-dev/test/jdk/java/io/Serializable/serialFilter/SerialFilterFunctionTest.java`、`SerialFilterFactoryTest.java`。
- `resolveObject/enableResolveObject` 替换用法 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（checkResolve 实现，RMI 依赖此机制替换远端引用）。
- GitHub 常见反序列化加固：`readClassDescriptor` 中比对 `ObjectStreamClass.lookup(local).getSerialVersionUID()` 与流中描述符（StackOverflow "ObjectInputStream readClassDescriptor serialVersionUID check" 方案）。

## 4. 测试用例设计 + 自动化实现（指向 ObjectInputStreamTest.java 黑盒 `testBlackBox`）
- 白盒: 类形态、extends InputStream、实现 ObjectInput+ObjectStreamConstants、protected 构造、readObject/getObjectInputFilter/setObjectInputFilter 为 final、方法面（含钩子）。
- 黑盒 16 组场景:
  1. 全字段往返 + transient 复位为默认值。
  2. 混合流：对象→readInt→EOF（available/skipBytes/readLine 边界）。
  3. GetField 类型化读取恢复值 + GetField 接口形态。
  4. registerValidation 按 priority 高→低执行；抛 InvalidObjectException 从 readObject 传出。
  5. resolveObject 默认不启用；enableResolveObject 返回先前状态；启用后替换生效。
  6. 过滤器：初始 null；设置后可见；二次设置 IllegalStateException；reject 过滤器 → readObject 抛 InvalidClassException("filter status: REJECTED")。
  7. OptionalDataException（length=4, eof=false）——对象读到原始块上。
  8. StreamCorruptedException（坏流头）。
  9. NotActiveException ×3（defaultReadObject/readFields/registerValidation）。
  10. 回引用 identity；readUnshared 读回引用 → InvalidObjectException。
  11/12. 写端中途异常 → 读端 WriteAbortedException（cause 链两次断言：自定义 IOException、NotSerializableException）。
  13. resolveClass 钩子收到流中描述符名字。
  14. 全套原始类型含 readUnsignedByte/readUnsignedShort。
  15. readFully/skipBytes 在块数据上。
  16. 关闭后 readObject/readInt 抛 IOException。
- 健壮性: 全内存流；不设置全局 serial filter factory；线程无关。

产出：`ObjectInputStreamTest.java`（白盒 ~32 + 黑盒 ~45 断言）。

## 深化追加（契约级）— 对象序列化核心（批次 ioI）

本批按 14 个契约维度深化读端行为，全部断言先在 JDK-8/27 双侧实测再固化。

### 覆盖维度
1. **基础往返全矩阵**：null；String（ASCII/中文/空/10k）；八大包装类含 MIN/MAX/NaN/-0.0/±Inf 位级；八原始字段 POJO；数组 byte[]/int[]/Object[]/String[]（含 null 元素、零长、int[2][3]）；ArrayList/HashMap/TreeMap/LinkedHashMap/HashSet/LinkedList/Properties/Date/BigInteger/BigDecimal；enum 按名往返 + 同一常量写两次读回 ==；String.class ==。
2. **transient 字段读回默认值；static 字段取读端当前值**（不随流传输）。
3. **共享引用与环**：反向引用读回 ==；菱形共享；父子环；同图两次序列化字节完全一致。
4. **readResolve**：返回 null → TC_NULL/null；抛 ObjectStreamException → 不包装直接透传。
5. **自定义 writeObject/readObject**：错误签名（如 readObject(Object)）不被识别为回调；写端 IOException → 读端 WriteAbortedException("writing aborted; …") 且 getCause() 保留原异常；RuntimeException 则不包装（读端见 EOFException）。
6. **Externalizable**：顺序为无参构造→readExternal；**没有公共无参构造时写端不检查（双侧实测写成功），读端 InvalidClassException「<类>; no valid constructor」**。
7. **serialVersionUID 字节级篡改**（见下方布局）→ InvalidClassException "local class incompatible"，消息内**两侧 UID 均为十进制**。
8. **流损坏**：坏 magic、TC_REFERENCE 截断、尾部垃圾字节、reset() 后句柄失效。
9. **NotSerializableException**：消息为非序列化类的 FQCN；非序列化父类的可序列化子类合法。
10. **协议 V1/V2**：同图字节长度不同；V2 块数据框架使未消费的自定义数据被自动跳过，V1 无框架未消费数据污染后续对象 → SCE；reset() 清反向引用表（流变长 + 身份重建）；OOS 构造器吞掉流头（探测）；DataOutput 交错写；close 后写入不抛（无内部关闭标志，仅底层 IOException 透传）。
11. **OIS 专有**：available()≥0；close() 幂等；裸 writeInt 后 readObject → OptionalDataException(eof=false,length=N)；越过自定义数据再读对象 → OptionalDataException(eof=true,length=0)；enableResolveObject（子类打开开关后替换生效）；writeUnshared/readUnshared 配对（非共享 !=；旧句柄失效 → InvalidObjectException）；registerValidation 优先级（高先）、回调时整图已完整、回调抛 InvalidObjectException 传播、null 回调实际行为；活动读写外 defaultReadObject/defaultReadObject/putFields/readFields → NotActiveException。

### serialVersionUID 字节布局（篡改实验依据）
```
AC ED 00 05                  流头（magic + 流版本）
73                           TC_OBJECT
72                           TC_CLASSDESC
xx xx                        类名长度（2 字节大端）
<类名>                       UTF-8 字节
xx xx xx xx xx xx xx xx      8 字节大端 serialVersionUID
xx                           flags（SC_SERIALIZABLE=0x02 | SC_WRITE_METHOD=0x01 | …）
xx xx                        可序列化字段数（2 字节大端）
<字段描述符 ×n>              类型码 1 字节 + 名长 2 字节 + 名（对象类型后接类型字符串）
78                           TC_ENDBLOCKDATA
70                           父类描述符（TC_NULL = 直接继承 Object）
<原始类型字段数据按字段名排序> <对象字段数据>
```
篡改实验：仅覆写第 8 字节段 UID → 读端 InvalidClassException，实测消息
`local class incompatible; stream classdesc serialVersionUID = 244837814094590, local class serialVersionUID = 200`
——0xDEADBEEFCAFE 打印为**十进制** 244837814094590（并非十六进制）；`getSerialVersionUID()` 与 `ObjectStreamClass.lookup(cls).getSerialVersionUID()` 一致。

### 负例矩阵（操作 | 异常 | 双侧消息子串）
| 操作 | 异常 | 消息子串（8/27 实测一致） |
|---|---|---|
| new OIS(魔数 00000000) | StreamCorruptedException | `invalid stream header: 00000000` |
| TC_REFERENCE 截断 | EOFException | （无消息） |
| 对象后接垃圾字节再读 | StreamCorruptedException | `invalid type code: 00` / `99` |
| UID 字节篡改 | InvalidClassException | `local class incompatible` + 两侧十进制 UID |
| Externalizable 无公共无参构造（读） | InvalidClassException | `; no valid constructor` |
| 图含非序列化对象 | NotSerializableException | FQCN 即消息 |
| 原始数据位置 readObject | OptionalDataException | eof=false, length=N |
| 越过自定义数据再读对象 | OptionalDataException | eof=true, length=0 |
| readUnshared 后旧句柄读 | InvalidObjectException | `cannot read back reference to unshared object` |
| 二次 readFields / readFields 后 defaultReadObject | NotActiveException | `not in readObject invocation or fields already read` |
| 写端回调抛 IOException | WriteAbortedException | `writing aborted; `，getCause 保留 |
| 协议 V1 下未消费数据后读对象 | StreamCorruptedException | `invalid type code: 00` |

### 版本差异（8 vs 27）
- **JEP 290 过滤器 API 仅 27 存在**：`getObjectInputFilter/setObjectInputFilter`；深度过滤器根对象计深度 1，拒绝 → InvalidClassException `filter status: REJECTED`；读过一个对象后再 set → IllegalStateException `filter can not be set after an object has been read`。JDK-8 反射探测 → NoSuchMethodException（作为基线断言）。
- 其余全部异常类型与消息子串逐字一致（Boolean 装箱值读回是新实例，不能断言 ==，双侧一致）。

产出：`ObjectInputStreamTest.java` 62 → 210 断言（含过滤器深度拒绝/置后读/数组 serialClass 断言；jtreg 通过；8 侧 45 → 167，过滤器以 NoSuchMethodException 基线替代）。
