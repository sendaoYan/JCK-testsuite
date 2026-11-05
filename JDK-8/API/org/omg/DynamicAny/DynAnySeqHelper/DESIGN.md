# org.omg.DynamicAny.DynAnySeqHelper

## API 功能
sequence&lt;DynAny&gt; typedef Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- tk_alias + content tk_sequence；**空数组可过 Any 通道**（零引用无需 marshal），非空本地实例数组 → MARSHAL。
