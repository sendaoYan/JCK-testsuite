# org.omg.PortableServer._ServantActivatorStub 契约覆盖设计（仅 JDK-8）

## API 功能
客户端桩：extends ObjectImpl implements ServantActivator；`_ids()` 两元素表。

## 使用方式
`new` 即可创建（未连 ORB）；经 Helper.narrow 收窄远程引用后调用。

## 参考来源
- OMG IDL-to-Java 桩规范；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. `_ids()` 精确 `["IDL:omg.org/PortableServer/ServantActivator:2.3", "IDL:omg.org/PortableServer/ServantManager:1.0"]`。
2. `_opsClass == ServantActivatorOperations.class`（且为接口）。
3. **负例**：未设 delegate 时 _get_delegate/incarnate/etherealize → `BAD_OPERATION` minor==0，
   消息精确 "The delegate has not been set!"。
4. 非 final、ObjectImpl+IDLEntity。
