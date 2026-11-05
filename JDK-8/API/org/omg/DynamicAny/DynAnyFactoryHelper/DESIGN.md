# org.omg.DynamicAny.DynAnyFactoryHelper

## API 功能
DynAnyFactory 对象引用 Helper。
## 使用方式
narrow resolve_initial_references 的返回值获得工厂。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确串；type tk_objref；narrow(null)→null；nil 引用 insert/extract；活工厂 narrow 幂等（== 同对象）。
