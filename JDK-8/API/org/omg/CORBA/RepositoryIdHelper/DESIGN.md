# org.omg.CORBA.RepositoryIdHelper 契约覆盖设计（仅 JDK-8）

## API 功能
IDL typedef Helper（alias 到 string）。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. id() 精确等于 IDL:omg.org/CORBA/<X>:1.0。2. type().kind()==tk_alias，content_type().kind()==tk_string。
3. write/read CDR 往返。4. insert/extract Any 往返。
5. 双通道等价：Helper.insert 后 any.extract_string() 同值——alias 插入后 Any 解包别名直接取原始类型。
6. 易错：alias 的 Any 直取按 content type 走（PolicyType→extract_ulong 而非 extract_long）。
