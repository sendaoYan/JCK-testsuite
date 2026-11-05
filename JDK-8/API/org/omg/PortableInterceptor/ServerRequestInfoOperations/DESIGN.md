# org.omg.PortableInterceptor.ServerRequestInfoOperations 契约设计

## API 功能
方法面接口：恰 11 方法 {sending_exception, object_id, adapter_id, server_id, orb_id, adapter_name, target_most_derived_interface, get_server_policy, set_slot, target_is_a, add_reply_service_context}。

## 参考来源
- OMG PI 规范；jdk8u372-b07 javap+反射实测。

## 测试用例设计
- 方法名集合精确相等（TreeSet 对照）；声明计数精确。
- 抽样方法签名：参数类型/返回类型/throws 精确。

