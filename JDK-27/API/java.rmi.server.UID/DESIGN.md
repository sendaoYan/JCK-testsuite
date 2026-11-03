# java.rmi.server.UID 行为测试设计

## 1. API 功能
- 构造器：`UID()`（主机内随时间唯一）、`UID(short num)`（well-known ID）。
- 查询/转换：`hashCode()`（`(int)time + (int)count`）、`equals(Object)`（比较 unique/time/count）、`toString()`（`unique:time:count` 十六进制）。
- 序列化：`write(DataOutput)` 写 int/long/short；`static UID read(DataInput)` 回读。

## 2. Java 用户如何使用
- `ObjID` 内部用 `UID` 作为地址空间标识。
- `new UID()` 生成 VM 内唯一 ID；`new UID((short)0)` 生成 well-known 标识。
- `uid.write(out)`/`UID.read(in)` 用 DataOutput/DataInput 编解码。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/UID.java`：`toString()` 为 `Integer.toString(unique,16)+":"+Long.toString(time,16)+":"+Integer.toString(count,16)`。
- JDK 源码 `ObjID.java`：`new ObjID(int)` 内部使用 `new UID((short)0)` 作为 well-known 地址空间。
- RMI 运行时 `sun.rmi.transport.*` 用 UID 生成 VMID/对象标识。

## 4. 测试用例设计 + 自动化实现
- 白盒：public final、Serializable。
- 黑盒：无参两实例不等；`UID((short)5)` 定值 equals/toString `"0:0:5"`/hashCode==5；非 UID/null 不等；DataOutput/DataInput 往返；well-known 往返；Java 序列化往返。
- 实现见 `UIDTest.java`。
