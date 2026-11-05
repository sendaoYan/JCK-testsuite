# org.omg.CORBA.StringHolder 契约覆盖设计（仅 JDK-8）

## API 功能
IDL string 出参 Holder：public `String value`（引用型，默认 null）；Streamable。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 三态。2. kind==tk_string + equal(get_primitive_tc(tk_string))。
3. wire 往返 hi/空串/含换行串。4. 负例：_write(null) → BAD_PARAM（jdk8u372 精确异常）。
5. 易错：null 与空串在 CDR 上语义不同，null 不可写。
