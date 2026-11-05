# org.omg.PortableServer.POAManager 契约覆盖设计（仅 JDK-8）

## API 功能
POA 管理器引用接口：本体无自有方法，extends `{POAManagerOperations, CORBA.Object, IDLEntity}`。

## 使用方式
控制所属 POA 的请求处理状态机（HOLDING/ACTIVE/DISCARDING/INACTIVE）。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法==0；extends 三接口顺序精确。
2. Proxy get_state() 路由 State.ACTIVE 证明行为面可用。
