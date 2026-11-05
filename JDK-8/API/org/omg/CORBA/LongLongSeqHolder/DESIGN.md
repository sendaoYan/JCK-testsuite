# org.omg.CORBA.LongLongSeqHolder 契约覆盖设计（仅 JDK-8）

## API 功能
sequence<long long> 出参 Holder：public `long[] value`。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 默认构造 value==null（数组引用型）。2. 带参构造存同数组引用。
3. `_type().kind()`==tk_alias（IDL typedef），equal(对应 Helper.type())。
4. `_write`→`_read` CDR 数组逐元素往返（Arrays.equals）。
5. 易错：序列 Holder 的 kind 是 tk_alias（typedef）不是 tk_sequence；W* 宽序列可带非 Latin-1 元素，窄序列不行。
