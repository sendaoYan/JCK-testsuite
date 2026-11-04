# java.rmi.server.UID 测试设计案

> 覆盖目标：完整契约（final + Serializable、no-arg 唯一性、well-known 构造器、equals/hashCode、toString、write/read round-trip）。

## 1. API 功能
- 构造：`UID()`（唯一）、`UID(short num)`（well-known）。
- `hashCode`/`equals`/`toString`；`write(DataOutput)`/`static read(DataInput)`。

## 2. 使用方式
- 作为地址空间标识；well-known UID 用固定 count。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/rmi/server/UID.html`）。
- OpenJDK `test/jdk/java/rmi/server/UID`；JCK `api/java_rmi/server/UID`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | final + Serializable | testStructure |
| P0 | no-arg 唯一、equals self | testUniqueness |
| P0 | well-known：相同 count 相等、不同不等、toString 十六进制格式 | testWellKnown |
| P1 | write/read round-trip（普通 + well-known） | testRoundTrip |

## 关键规范细节
- well-known 构造器 `unique=0, time=0, count=num`，因此 `toString()` 为 `"0:0:<num>"`（十六进制），例如 `new UID((short)7).toString()` == `"0:0:7"`。
- `hashCode() = (int)time + (int)count`（well-known 时等于 count）。
