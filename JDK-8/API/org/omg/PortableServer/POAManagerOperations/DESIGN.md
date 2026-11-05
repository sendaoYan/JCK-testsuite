# org.omg.PortableServer.POAManagerOperations 契约覆盖设计（仅 JDK-8）

## API 功能
状态机操作面：activate/hold_requests(boolean)/discard_requests(boolean)/deactivate(boolean,boolean)/get_state()。

## 使用方式
4 个迁移方法全部 throws AdapterInactive；get_state 返回 POAManagerPackage.State。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 恰 5 方法、方法名集合精确、无父接口。
2. 每个 mutate 方法的异常表恰为 AdapterInactive 单元素。
3. 参数个数精确：activate()=0、hold/discard=1、deactivate=2。
