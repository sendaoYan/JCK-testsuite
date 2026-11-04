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
