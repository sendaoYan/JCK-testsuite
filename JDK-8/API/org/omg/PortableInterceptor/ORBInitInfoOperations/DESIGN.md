# org.omg.PortableInterceptor.ORBInitInfoOperations 契约设计

## API 功能
方法面接口：恰 10 方法 {arguments, orb_id, codec_factory, register_initial_reference, resolve_initial_references, add_client_request_interceptor, add_server_request_interceptor, add_ior_interceptor, allocate_slot_id, register_policy_factory}。

## 参考来源
- OMG PI 规范；jdk8u372-b07 javap+反射实测。

## 测试用例设计
- 方法名集合精确相等（TreeSet 对照）；声明计数精确。
- 抽样方法签名：参数类型/返回类型/throws 精确。
- codec_factory() 返回 org.omg.IOP.CodecFactory（跨模块）；add_*_interceptor throws DuplicateName；resolve_initial_references throws InvalidName。
