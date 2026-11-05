# org.omg.CORBA.ShortHolder 契约覆盖设计（仅 JDK-8）

## API 功能
IDL short 出参 Holder：public `short value`；Streamable。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 三态（MIN/MAX）。2. kind==tk_short + equal。3. wire 往返 ±300。4. Any 通道 insert_short。
5. 易错：Short.MIN_VALUE 需 short 字面量。
