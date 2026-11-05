# org.omg.CosNaming.NamingContext

## API 功能
命名上下文接口：bind/rebind/resolve/unbind/list/new_context/destroy 等命名操作。
## 使用方式
CORBA 对象引用接口（Operations + CORBA.Object + IDLEntity）；经 NamingContextHelper.narrow 获得。
## 参考来源
JDK-8 rt.jar 接口形态；CosNaming 规范；实测 jdk8u372-b07（javap+反射）。
## 测试用例设计 + 易错细节
- 接口层级精确（extends Operations + Object + IDLEntity）。
- 10 个方法名全表存在；bind 的 4 个 checked 异常签名精确断言。
