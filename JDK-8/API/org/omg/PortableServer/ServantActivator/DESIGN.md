# org.omg.PortableServer.ServantActivator 契约覆盖设计（仅 JDK-8）

## API 功能
RETAIN servant 管理器引用接口：本体无自有方法，extends `{ServantActivatorOperations, ServantManager, IDLEntity}`。

## 使用方式
 incarnate/etherealize 回调由 POA 在 AOM 激活/去活时调用。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法==0；extends 三接口顺序精确（注意中间是 ServantManager 而非 CORBA.Object）。
2. Proxy incarnate/etherealize 按调用顺序路由。
