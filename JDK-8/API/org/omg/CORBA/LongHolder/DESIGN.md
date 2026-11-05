# org.omg.CORBA.LongHolder 契约覆盖设计（仅 JDK-8）

## API 功能
IDL long long 出参/入参 Holder：public `long value`；实现 Streamable（`_read/_write/_type`），final 类。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；Holder 用默认/带参构造 + `_read`/`_write`/`_type` 三方法；Helper 为全静态面（id/type/insert/extract/read/write）。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-b，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 CORBA IDL→Java 映射规范
## 测试用例设计 + 易错细节
1. 三态：默认 0L（Java long 类型零值）；带参构造回读（MIN/MAX）；value 可写回读。
2. wire 通道：`_write`→`_read` 经 `orb.create_output_stream()` 真往返（正/负值、同流双 Holder 顺序往返）。
3. `_type()` 精确：kind==tk_longlong 且 equal(get_primitive_tc(tk_longlong))——IDL long long 映射 Java long。
4. Any 通道：insert_longlong 后 holder._read(any.create_input_stream()) 回读。
5. 负例：LongHolder(tk_longlong)._read 去读 4 字节 tk_long Any 流 → MARSHAL（宽度不匹配 underflow）。
6. 易错：基本型 Holder 默认值是类型零值不是 null；IntHolder 才是 tk_long（CORBA 无 tk_int）。
7. 易错：本 JDK 无 LongHelper——基本型只有 Holder 通道 + Any 直取通道，别按 Helper 类名写测试。
