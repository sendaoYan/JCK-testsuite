# java.io.ObjectOutputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(OutputStream)`、`protected ()`（子类用）。
- 对象输出：`writeObject`、`writeUnshared`、`defaultWriteObject`、`putFields`+`writeFields`、`reset`、`useProtocolVersion`。
- 原始数据（DataOutput 全套）：`write/writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble/writeUTF/writeBytes/writeChars`。
- 生命周期：`flush`、`close`。
- 子类钩子（protected）：`writeStreamHeader`、`writeClassDescriptor`、`annotateClass/annotateProxyClass`、`replaceObject`+`enableReplaceObject`、`writeObjectOverride`。

## 2. Java 用户如何使用
- `try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(obj); }` 标准写法。
- 缓存/去重：同一对象两次 writeObject 产生**回引用**（同一句柄）；`reset()` 清句柄表强制重写。
- 自定义类内 `private void writeObject(ObjectOutputStream out){ out.defaultWriteObject(); out.writeInt(extra); }` 追加自定义数据；或 `PutField` 手工逐字段写。
- 子类 `enableReplaceObject(true)` + `replaceObject` 做代理/加密替换。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectOutputStream out = new ObjectOutputStream(baos); out.writeObject(...)` — `jdk8u-dev/jdk/test/java/io/Serializable/InvalidClassException/noargctor/Serialize/SubclassAcrossPackage.java:181`。
- `oout.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);` — `jdk8u-dev/jdk/test/java/io/Externalizable/compatibility/ExternalizableBlockData.java:79`。
- `writeUnshared`/回引用/reset 行为 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectOutputStream.java`（writeObject0/replaceObject 实现）。

## 4. 测试用例设计 + 自动化实现（ObjectOutputStreamTest.java，纯黑盒）
黑盒 15 组场景:
1. 全类型字段往返（8 种基本类型+String+数组），transient 不落盘。
2. 重复写同对象 → 回引用同一 identity；reset 后强制新副本。
3. writeUnshared 两次 → 两个相等但不同 identity 的副本。
4. 全套 DataOutput 原始数据块往返 + available 分帧感知 + 流耗尽。
5. custom writeObject：defaultWriteObject + 尾随自定义 writeInt 由对端读回。
6. PutField.put 转换值写入（i*2）+ 对端 readFields 读回 42。
7. NotActiveException ×3（defaultWriteObject/putFields/writeFields 在活动写之外）。
8. NotSerializableException（message=类名）。
9. replaceObject 替换落流对象；annotateClass 被调用并记录类名。
10. useProtocolVersion(PROTOCOL_VERSION_1) 往返。
11. Class 对象写读往返（TC_CLASS）。
12. flush/close 幂等。
13. 裸读流头 == STREAM_MAGIC/STREAM_VERSION。
14. ObjectOutput 接口视图多态使用。
15. 与 ObjectStreamClass.lookup 的 SUID 一致。
- 健壮性: 全内存流；identity 断言基于机制保证（回引用/Unshared/reset），确定性。JDK-8 为黑盒（无反射白盒，无 @Serial 注解）。

## 深化追加（契约级）— ObjectOutputStream 写端契约（批次 ioI）

### 覆盖维度（W1–W29）
1. **基础写出矩阵**：null/字符串/包装类极值/原始 POJO/数组/集合/enum/Class 与 OIS 读端矩阵对偶；写出字节确定性（同图两次一致）。
2. **transient 不写出**（流中无该字段，读端得默认）；**static 不写出**（读端取读时值）。
3. **共享与环**：同一对象写两次 → TC_REFERENCE 反向引用（读端 ==）；父子环成功；`reset()` 清空句柄表 → 同对象再写产生新副本（字节变长、读端 !=）。
4. **writeReplace**：替换对象获得句柄（读端共享）；返回 null → TC_NULL；抛 ObjectStreamException → 不包装直接透传（读端看到同一异常）；替换结果再被引用仍共享。
5. **自定义 writeObject**：`defaultWriteObject` + 追加原始数据 与读端配对；错误签名方法不被调用（字段照常写出）；回调抛 IOException → 读端 WriteAbortedException（getCause 保留）；抛 RuntimeException → 不包装（读端 EOFException）；writeObject 内 putFields 子集写出。
6. **Externalizable**：writeExternal 原始写出；**写端不检查公共无参构造**（无该构造也写成功——双侧实测）；readExternal 抛 IOException 同样进入 abort 语义。
7. **writeUnshared**：不注册句柄；之后 writeObject 同一对象仍可再写（两侧句柄独立）。
8. **协议 V1/V2**：`useProtocolVersion` 后同图字节长度不同（V1 更短）；V1 写 + V2 读在未消费自定义数据处损坏（SCE `invalid type code: 00`），V2 写自动跳过未消费块。
9. **close 语义**：OOS 无内部关闭标志——close 后 writeObject/writeInt/flush 全部成功（BAOS 收到字节）；只有底层流的 IOException 透传（ throwing sink 实测）。
10. **子类钩子**：`writeObjectOverride`（子类构造走 protected ObjectOutputStream() 时不写流头——探测确认无 AC ED）；`annotateClass` 对每个**不同**类恰好调用一次、同类第二次走引用不再调用；`replaceObject`（enableReplaceObject 打开后生效，未打开时替换不发生）。
11. **PutField 写端契约**（详见 PutField DESIGN）：子集写出零值缓冲、静态/大小写不匹配字段名拒绝、putFields() 缓冲同一性、双重 writeFields() 流损坏、deprecated write(owner)。

### 负例矩阵（操作 | 异常 | 双侧消息子串）
| 操作 | 异常 | 消息子串（8/27 实测一致） |
|---|---|---|
| writeObject(未实现 Serializable) | NotSerializableException | FQCN 即消息 |
| 非序列化父类 + 序列化子类写出 | （成功） | — |
| useProtocolVersion(99) | IllegalArgumentException | `unknown version: 99` |
| 已写出数据后 useProtocolVersion | IllegalStateException | `stream non-empty` |
| 流外 defaultWriteObject/putFields | NotActiveException | `not in call to writeObject` |
| 无 putFields 直接 writeFields | NotActiveException | `no current PutField object` |
| PutField.write(非属主流) | IllegalArgumentException | `wrong stream` |
| 回调抛 IOException | 读端 WriteAbortedException | `writing aborted; ` |
| 双重 writeFields() | 写成功；读端 SCE | `invalid type code: 00` |
| deprecated write(owner) 带原始字段 | 写成功；读端 SCE | `invalid type code: 00` |

### 版本差异（8 vs 27）
- 消息与异常类型全部一致；27 侧新增 `@Serial` 注解标注 UID（行为等价）。
- `putFields()`/`writeFields()` 行为、协议 V1/V2 字节布局一致。

产出：`ObjectOutputStreamTest.java` 28 → 159 断言（工作树起点 29；jtreg 双侧通过；27 侧 35 → 189）。
