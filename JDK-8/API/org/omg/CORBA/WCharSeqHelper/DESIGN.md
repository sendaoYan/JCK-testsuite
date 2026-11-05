# org.omg.CORBA.WCharSeqHelper 契约覆盖设计（仅 JDK-8）

## API 功能
sequence<wchar> 的 Helper。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. id() 精确等于 IDL:omg.org/CORBA/<X>:1.0。2. type().kind()==tk_alias。
3. write/read CDR 往返（Arrays.equals 逐元素）。4. insert/extract Any 往返。
5. 易错：序列 Helper 的 repository id 是 typedef 自身的 id，与元素类型无关。
