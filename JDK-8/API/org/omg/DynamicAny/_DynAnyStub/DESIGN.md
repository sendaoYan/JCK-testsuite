# org.omg.DynamicAny._DynAnyStub

## API 功能
DynAny 根类型的客户端桩。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- _ids() **仅一个** id（根类型无基类）；离线 type()/component_count() → BAD_OPERATION（无 delegate）。
