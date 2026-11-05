# org.omg.PortableServer.portable.Delegate 契约覆盖设计（仅 JDK-8）

## API 功能
Servant 的 ORB 侧委托接口：8 个方法（orb/this_object/poa/object_id/default_POA/is_a/non_existent/get_interface_def）。

## 使用方式
ORB 内部实现注入 Servant（`Servant._set_delegate`）；测试用自定义实现验证全通道路由。

## 参考来源
- OMG POA portable Delegate；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 恰 8 方法、全抽象、无父接口、无 checked 异常；方法名集合精确锁定。
2. 返回类型逐个精确（ORB/Object/POA/byte[]/POA/boolean/boolean/Object）。
3. is_a 是唯一双参方法（Servant, String）。
