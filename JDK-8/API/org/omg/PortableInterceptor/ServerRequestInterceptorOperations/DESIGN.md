# org.omg.PortableInterceptor.ServerRequestInterceptorOperations 契约设计

## API 功能
方法面接口：恰 5 方法 {receive_request_service_contexts, receive_request, send_reply, send_exception, send_other}。

## 参考来源
- OMG PI 规范；jdk8u372-b07 javap+反射实测。

## 测试用例设计
- 方法名集合精确相等（TreeSet 对照）；声明计数精确。
- 抽样方法签名：参数类型/返回类型/throws 精确。

