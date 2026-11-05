# org.omg.PortableServer.POAOperations 契约覆盖设计（仅 JDK-8）

## API 功能
POA 全操作面：32 方法 / 31 个不同名（唯 the_activator 重载 getter+setter）；无父接口。

## 使用方式
create_POA/find_POA/activate_object(…)/servant_to_* /reference_to_* /id_to_* /the_* 属性/7 个策略工厂。

## 参考来源
- OMG POA IDL；jdk8u372-b07 javap（全表逐条比对）

## 测试用例设计 + 易错细节
1. 计数锁 32/31；20 个代表方法名 contains 断言。
2. checked 异常面：create_POA 恰 {AdapterAlreadyExists, InvalidPolicy}；get_servant 恰 {NoServant, WrongPolicy}。
3. the_activator 双形态返回类型精确（getter AdapterActivator / setter void）。
4. 7 个 create_*_policy 工厂各自映射到对应 Policy 接口（抽查 2 个返回类型）。
