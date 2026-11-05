# org.omg.PortableInterceptor.RequestInfoOperations 契约设计

## API 功能
方法面接口：恰 14 方法 {request_id, operation, arguments, exceptions, contexts, operation_context, result, response_expected, sync_scope, reply_status, forward_reference, get_slot, get_request_service_context, get_reply_service_context}。

## 参考来源
- OMG PI 规范；jdk8u372-b07 javap+反射实测。

## 测试用例设计
- 方法名集合精确相等（TreeSet 对照）；声明计数精确。
- 抽样方法签名：参数类型/返回类型/throws 精确。
- reply_status 对应 reply_status 常量族；service context 返回 IOP.ServiceContext。
