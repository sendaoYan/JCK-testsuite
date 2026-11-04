# java.rmi.server.ObjID 行为测试设计

## 1. API 功能
- 构造器：`ObjID()`（生成主机内唯一 ID）、`ObjID(int objNum)`（生成 well-known ID）。
- 查询/转换：`hashCode()`（返回对象号）、`equals(Object)`（比较 objNum + space）、`toString()`。
- 序列化：`write(ObjectOutput)` 写 objNum + UID；`static ObjID read(ObjectInput)` 回读。
- 常量：`REGISTRY_ID=0`、`ACTIVATOR_ID=1`、`DGC_ID=2`。

## 2. Java 用户如何使用
- `new ObjID()` 用于导出远程对象时分配对象号；`new ObjID(42)` 用于 well-known 对象。
- `objid.write(out)` / `ObjID.read(in)` 在 stub 中编解码对象标识。
- `equals`/`hashCode` 用于把远程对象放入 `Hashtable` 等。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/ObjID.java`：`hashCode()` 返回 `(int) objNum`，`toString()` 返回 `"[" + space(可选) + objNum + "]"`。
- JDK 测试 `test/jdk/java/rmi/server/ObjID/randomIDs/RandomIDs.java`：`new ObjID().write(new ObjectOutputStream(...))` 校验随机 ID 的序列化。
- JDK 测试 `test/jdk/java/rmi/registry/interfaceHash/InterfaceHash.java`：用 `ObjID` 标识注册表对象。

## 4. 测试用例设计 + 自动化实现
- 白盒：public final、Serializable、三常量值。
- 黑盒：无参唯一（两实例不等）；`ObjID(42)` 定值 equals/hashCode==42/toString `"[0:0:0, 42]"`；非 ObjID/null 不等；marshal write/read 往返；Java 序列化往返。
- 实现见 `ObjIDTest.java`。
