# org.omg.CORBA.ServerRequest 契约覆盖设计（仅 JDK-8）

## API 功能
DSI 服务端请求抽象类：恰好 9 个方法名，其中仅 ctx() 为抽象；result(Any)/set_result(Any) 均为 void setter，无 result() getter；except(Any) 取 Any（javap 实测）。

## 使用方式
经 `ORB.init(new String[0], null)` 离线 ORB 实例化/驱动（无网络、无命名服务、无 orb.run()）；抽象类经 ORB 工厂或匿名子类通道；接口/桩经反射方法面对照 javap。

## 参考来源
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-a，2026-11-05）
- javap -public 枚举完整公开 API
- OpenJDK jdk8u org.omg.CORBA 源码与 IDL→Java 映射规范

## 测试用例设计 + 易错细节
1. 见 <Simple>Test.java 中 'deepened' 注释段：形态/行为/负例三层，断言先实测后写入。
2. 负例精确异常表（jdk8u372 实测）：
   - ORB: string_to_object(bogus)→BAD_PARAM(OMGVMCID|7, COMPLETED_NO)、connect/disconnect(null)→OBJ_ADAPTER(SUN 202)、get_default_context/get_current/work_pending/perform_work/create_operation_list/get_service_information→NO_IMPLEMENT；object_to_string(null) 不抛返回 IOR: 串。
   - Any: extract 类型错→BAD_OPERATION(SUN 206)、空 any→BAD_OPERATION(SUN 207)。
   - TypeCode: 成员下标越界→TypeCodePackage.Bounds、kind 不适用→BadKind。
   - NVList/ContextList/ExceptionList: 下标越界（含 -1）→org.omg.CORBA.Bounds。
   - LocalObject: 19 个远程方法→NO_IMPLEMENT('This is a locally constrained object.')。
   - ObjectImpl/桩族: 无 delegate→BAD_OPERATION('The delegate has not been set!')。
   - IDL 枚举 from_int 越界/负数→BAD_PARAM。
3. 易错点：见默认清单；ORB 测试绝不调 run()/poll_next_response()/list_initial_services()。
