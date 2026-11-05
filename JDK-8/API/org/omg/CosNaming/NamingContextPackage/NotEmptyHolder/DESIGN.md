# org.omg.CosNaming.NamingContextPackage.NotEmptyHolder

## API 功能
异常 Holder（out/inout）。
## 使用方式
默认 value==null；_read/_write；_type() tk_except 且 id 精确。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- final+Streamable；wire 往返保类、消息回到 repoId 形态。
