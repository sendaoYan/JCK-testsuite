# org.omg.PortableServer.AdapterActivatorOperations 契约覆盖设计（仅 JDK-8）

## API 功能
单回调面：`boolean unknown_adapter(POA parent, String name)`；无父接口、无 checked 异常。

## 使用方式
服务端实现该回调即可（接口小到可匿名实现）。

## 参考来源
- OMG POA；jdk8u372-b07 javap

## 测试用例设计 + 易错细节
1. 恰 1 方法；签名（POA,String)->boolean 精确；无父接口；异常表空。
