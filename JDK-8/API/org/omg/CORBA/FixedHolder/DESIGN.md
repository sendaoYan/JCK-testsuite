# org.omg.CORBA.FixedHolder 契约覆盖设计（仅 JDK-8）

## API 功能
IDL fixed 出参 Holder：public `java.math.BigDecimal value`（引用型，默认 null——不是 0）；Streamable。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 三态：默认 null 是易错点（BigDecimal 引用型）。2. kind==tk_fixed + equal。
3. wire 往返：123.45 读回 12345 scale 0——CDR fixed 编码不携带 scale（尺度在 TypeCode 中），往返丢 scale 是规范行为。
4. 7 往返保真。5. 断言用 compareTo+scale，不用 equals（equals 严格含 scale）。
