# org.omg.CORBA.ObjectHolder 契约覆盖设计（仅 JDK-8）

## API 功能
IDL Object 出参 Holder：public `org.omg.CORBA.Object value`（默认 null）；Streamable。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 三态。2. kind==tk_objref + equal(ObjectHelper.type())。3. nil objref wire 往返读回 null。
4. ObjectHelper.insert(null)/extract 双通道返回 null 且 any kind==tk_objref。
5. 易错：写的是 nil objref（IOR type id + 空 profile），不是零字节。
