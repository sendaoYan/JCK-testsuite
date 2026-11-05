# org.omg.CORBA.IntHolder 契约覆盖设计（仅 JDK-8）

## API 功能
IDL long 出参 Holder：public `int value`；Streamable；final；kind 为 tk_long（CORBA 无 tk_int）。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 三态（MIN/MAX/value 可写）。2. kind==tk_long + equal 原始 TypeCode。
3. wire 往返（-5/900）。4. Any 通道 insert_long→_read。
5. 易错：IntHolder(tk_long) 与 LongHolder(tk_longlong) 的 kind 差异是本批核心对照点。
