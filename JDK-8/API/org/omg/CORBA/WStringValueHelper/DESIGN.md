# org.omg.CORBA.WStringValueHelper 契约覆盖设计（仅 JDK-8）

## API 功能
boxed valuetype `wstring WStringValue` 的 Helper + BoxedValueHelper 实例面。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. id 精确，get_id 同值。2. kind==tk_value_box，content==tk_wstring。
3. write/read 空串/你好/a\nb 往返——宽字符串走宽编码不受 ISO-8859-1 限制。
4. write_value/read_value 宽字符往返。5. insert/extract 你好 Any 往返 + any kind==tk_value_box。
6. 负例一：insert 后 any.extract_wstring() → BAD_OPERATION（kind 是 value_box 非 wstring）。
7. 负例二：extract(tk_long any) → MARSHAL。
