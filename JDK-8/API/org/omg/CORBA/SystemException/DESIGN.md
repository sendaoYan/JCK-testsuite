# org.omg.CORBA.SystemException 契约覆盖设计（仅 JDK-8）

## API 功能
所有标准系统异常的公共抽象基类（extends RuntimeException，public int minor + public CompletionStatus completed，toString 为 OMG 格式 'name: msg  vmcid: X  minor code: N  completed: C'，非空消息会嵌入；minor 按高 20 位 vmcid + 低 12 位拆分渲染（12345→vmcid 0x3000/minor 57），OMGVMCID 高位渲染 'vmcid: OMG'）。本目录含 39 个具体异常类的批量反射断言：默认 minor=0/completed=COMPLETED_NO（jdk8u372 实测，非 COMPLETED_MAYBE）、默认构造 getMessage() 为空串、(String) 构造消息回读、全参构造回读与 toString；OMGVMCID 高位 minor 渲染为 'vmcid: OMG'。

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
