# org.omg.PortableInterceptor.ClientRequestInterceptorOperations 契约设计

## API 功能
方法面接口：恰 5 方法 {send_request, send_poll, receive_reply, receive_exception, receive_other}。

## 参考来源
- OMG PI 规范；jdk8u372-b07 javap+反射实测。

## 测试用例设计
- 方法名集合精确相等（TreeSet 对照）；声明计数精确。
- 抽样方法签名：参数类型/返回类型/throws 精确。
- send_request/receive_exception/receive_other 声明 ForwardRequest；send_poll/send_reply 无异常。
