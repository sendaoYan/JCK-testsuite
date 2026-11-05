# org.omg.PortableInterceptor.ClientRequestInfoOperations 契约设计

## API 功能
方法面接口：恰 9 方法 {target, effective_target, effective_profile, received_exception, received_exception_id, get_effective_component, get_effective_components, get_request_policy, add_request_service_context}。

## 参考来源
- OMG PI 规范；jdk8u372-b07 javap+反射实测。

## 测试用例设计
- 方法名集合精确相等（TreeSet 对照）；声明计数精确。
- 抽样方法签名：参数类型/返回类型/throws 精确。

