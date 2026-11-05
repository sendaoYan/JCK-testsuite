# org.omg.PortableServer.AdapterActivator 契约覆盖设计（仅 JDK-8）

## API 功能
适配器激活器引用接口：本体无自有方法，extends `{AdapterActivatorOperations, CORBA.Object, IDLEntity}`。

## 使用方式
注册到 POA；find_POA(name, activate=true) 未命中时回调 unknown_adapter(parent, name) 现场建子 POA。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法==0；extends 三接口顺序精确。
2. Proxy unknown_adapter 路由 true（回调通道）。
