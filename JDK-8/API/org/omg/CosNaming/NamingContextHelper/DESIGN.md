# org.omg.CosNaming.NamingContextHelper

## API 功能
NamingContext 对象引用 Helper：insert/extract、read/write（nil 引用）、narrow、type()/id()。
## 使用方式
静态方法；客户端 narrow 后调用命名操作。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id 精确串 `IDL:omg.org/CosNaming/NamingContext:1.0`；type 为 tk_objref、name/id 对应。
- nil 引用三通道一致：insert(null)/extract→null、write(null)/read→null、narrow(null)→null（不抛）。
- 负例：tk_long Any 上 extract → MARSHAL。
