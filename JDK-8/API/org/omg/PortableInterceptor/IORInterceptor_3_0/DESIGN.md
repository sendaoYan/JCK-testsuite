# org.omg.PortableInterceptor.IORInterceptor_3_0 契约设计

## API 功能
PI conformance 接口（方法在对应 *Operations 接口）。

## 参考来源
- OMG PI 规范；jdk8u372-b07 反射实测。

## 测试用例设计
- 声明 0 方法；父接口列表精确（Operations + CORBA.Object/Interceptor/RequestInfo + IDLEntity 按实际）。
- 传递继承断言（如 IS-A Interceptor / RequestInfoOperations）。

## 易错规范细节
3.0 扩展：components_established/adapter_manager_state_changed/adapter_state_changed；IS-A IORInterceptor。
