# org.omg.PortableServer._ServantLocatorStub 契约覆盖设计（仅 JDK-8）

## API 功能
客户端桩：extends ObjectImpl implements ServantLocator；`_ids()` 两元素表。

## 使用方式
同 _ServantActivatorStub，用于 NON_RETAIN 管理器远程调用。

## 参考来源
- OMG IDL-to-Java 桩规范；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. `_ids()` 精确 `["IDL:omg.org/PortableServer/ServantLocator:1.0", "IDL:omg.org/PortableServer/ServantManager:1.0"]`
   ——**自有 id 是 1.0**（ServantActivator 却是 2.3，POA 规范收编版本不一致，易错）。
2. `_opsClass == ServantLocatorOperations.class`。
3. **负例**：未设 delegate 时 preinvoke/postinvoke → `BAD_OPERATION` minor==0 消息精确 "The delegate has not been set!"。
