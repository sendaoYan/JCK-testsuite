# org.omg.CORBA.StringValueHelper 契约覆盖设计（仅 JDK-8）

## API 功能
boxed valuetype `string StringValue` 的 Helper + BoxedValueHelper 实例面（get_id/read_value/write_value）。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. id() 精确 IDL:omg.org/CORBA/StringValue:1.0，instance get_id() 同值。2. type kind==tk_value_box，content==tk_string。
3. write/read 空串/hello/a\nb CDR 往返。4. write_value/read_value 通道往返。
5. 负例一：write(你好) → DATA_CONVERSION（窄字符串 CDR 默认 ISO-8859-1 无法映射）。
6. 负例二：insert(any,..) 离线 → MARSHAL（SUN 257 无 boxed StringValue 值类，jdk8u372 quirk）。
7. 负例三：extract(tk_long any) → MARSHAL。8. 与 WStringValueHelper 的行为差异是核心对照。
