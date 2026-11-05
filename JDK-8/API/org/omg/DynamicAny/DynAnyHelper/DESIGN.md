# org.omg.DynamicAny.DynAnyHelper

## API 功能
DynAny 对象引用 Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id/type/narrow/nil 三件套；**负例**：本地（未连接 ORB）DynAny 过 Any → MARSHAL（无法序列化对象引用）——离线闭环的重要边界。
