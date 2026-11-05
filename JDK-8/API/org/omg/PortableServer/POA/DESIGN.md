# org.omg.PortableServer.POA 契约覆盖设计（仅 JDK-8）

## API 功能
POA 对象引用接口：本体无自有方法，extends `{POAOperations, CORBA.Object, IDLEntity}`（顺序精确）。

## 使用方式
服务端经 RootPOA/子 POA 操作对象生命周期；本用例用 Proxy 验证方法面可路由。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法数==0；extends 三接口声明顺序精确（Operations 在前、IDLEntity 在后）。
2. POAOperations 面==32（见 POAOperations 用例）。
3. Proxy 行为面：the_name/id/the_children 路由证明；POA 引用即 CORBA Object。
