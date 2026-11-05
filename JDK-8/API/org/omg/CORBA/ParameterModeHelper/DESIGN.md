# org.omg.CORBA.ParameterModeHelper 契约覆盖设计（仅 JDK-8）

## API 功能
IDL 枚举 Helper（kind==tk_enum）。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. id() 精确。2. type().kind()==tk_enum。3. write/read 往返到规范常量实例（== 恒等，非仅 equals）。
4. insert/extract Any 往返 PARAM_OUT 同为恒等常量。5. 附带锁 ParameterMode 常量表 {0,1,2}、from_int(1) 归一、from_int(9)→BAD_PARAM。
6. 易错：枚举 Helper 读回的是 from_int 归一后的单例常量，可用 == 断言。
