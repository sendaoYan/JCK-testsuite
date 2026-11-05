# org.omg.PortableServer.ServantActivatorPOA 契约覆盖设计（仅 JDK-8）

## API 功能
服务端 tie 基类（abstract，extends Servant implements ServantActivatorOperations+InvokeHandler）。

## 使用方式
匿名/命名子类实现 incarnate/etherealize；`_this(orb)` 换取可分发引用。

## 参考来源
- OMG POA tie 模型；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. `_all_interfaces(null,null)` 精确 `["IDL:omg.org/PortableServer/ServantActivator:2.3", ".../ServantManager:1.0"]`。
2. 未关联 `_this()` → BAD_INV_ORDER（消息同 Servant）；`_this(orb)` 离线返回 IOR 引用且 `_orb()==orb`。
3. `_invoke("bogusop"/"incarnate", 空流, null)` → `BAD_OPERATION` minor==0。
