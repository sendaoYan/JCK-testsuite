# java.rmi.server.ObjID 测试设计案

> 覆盖目标：完整契约（final + Serializable、三个常量、no-arg 唯一、well-known、hashCode==objNum、toString、write/read round-trip）。

## 1. API 功能
- 常量：`REGISTRY_ID=0`、`ACTIVATOR_ID=1`、`DGC_ID=2`。
- 构造：`ObjID()`（唯一）、`ObjID(int objNum)`（well-known）。
- `hashCode`/`equals`/`toString`；`write(ObjectOutput)`/`static read(ObjectInput)`。

## 2. 使用方式
- 远程对象导出时的对象标识。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/rmi/server/ObjID.html`）。
- OpenJDK `test/jdk/java/rmi/server/ObjID`；JCK `api/java_rmi/server/ObjID`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量 + final + Serializable | testConstantsAndStructure |
| P0 | no-arg 唯一、equals self | testUniqueness |
| P0 | well-known：相等、hashCode==objNum、toString 格式、null/非本类 | testWellKnown |
| P1 | write/read round-trip（普通 + well-known） | testRoundTrip |

## 关键规范细节
- well-known `ObjID` 的 space 为 `new UID((short)0)`，故 `new ObjID(7).toString()` == `"[0:0:0, 7]"`，`hashCode() == 7`（`(int)objNum`）。
- no-arg `ObjID` 的 space 是共享本地地址空间，toString 省略 space（形如 `[<objNum>]`）。
