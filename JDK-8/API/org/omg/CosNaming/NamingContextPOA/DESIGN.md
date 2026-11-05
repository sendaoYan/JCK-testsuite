# org.omg.CosNaming.NamingContextPOA

## API 功能
POA 服务端骨架（Servant 路线，取代 ImplBase）：实现对应 Operations + InvokeHandler。
## 使用方式
继承实现业务方法；_invoke 分发请求；_this() 取对象引用（需 POA 环境，测试不调用）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- abstract + extends org.omg.PortableServer.Servant + implements Operations/InvokeHandler 四件套。
- 公共无参构造存在；声明 _invoke/_all_interfaces/_this。
