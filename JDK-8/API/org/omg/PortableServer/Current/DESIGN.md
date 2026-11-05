# org.omg.PortableServer.Current 契约覆盖设计（仅 JDK-8）

## API 功能
POA Current 引用接口：本体无自有方法，extends `{CurrentOperations, CORBA.Current, IDLEntity}`。

## 使用方式
 servants 在调用上下文内查询自己所属 POA/object id（get_POA/get_object_id，越上下文抛 NoContext）。

## 参考来源
- OMG POA Current；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法==0；extends 三接口顺序精确；CORBA.Current extends {CurrentOperations, Object, IDLEntity}（顺序精确）。
2. Proxy get_POA/get_object_id 路由证明。
