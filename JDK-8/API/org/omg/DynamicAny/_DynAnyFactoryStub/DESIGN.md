# org.omg.DynamicAny._DynAnyFactoryStub

## API 功能
DynAnyFactory 客户端桩。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- `new` 不抛；_ids() 单元素精确 `IDL:omg.org/DynamicAny/DynAnyFactory:1.0`；离线操作 → BAD_OPERATION（本地即抛）。
