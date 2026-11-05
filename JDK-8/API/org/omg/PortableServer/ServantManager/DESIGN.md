# org.omg.PortableServer.ServantManager 契约覆盖设计（仅 JDK-8）

## API 功能
servant 管理器标记接口：本体与 Operations 均无方法，extends `{ServantManagerOperations, CORBA.Object, IDLEntity}`。

## 使用方式
RETAIN 用 ServantActivator、NON_RETAIN 用 ServantLocator；本接口是 set_servant_manager 的静态类型。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法==0；extends 三接口顺序精确。
2. Proxy 可作 CORBA Object 使用（标记面仍可实例化）。
