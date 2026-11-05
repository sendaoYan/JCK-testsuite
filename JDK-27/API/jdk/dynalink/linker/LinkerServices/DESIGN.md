# jdk.dynalink.linker.LinkerServices 设计案

## 1. API 功能
链接服务接口：asType/canConvert/getGuardedInvocation 等。

## 2. Java 用户如何使用
DynamicLinker.getLinkerServices()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/LinkerServices.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 转换服务真值表：canConvert 按方法调用转换（String→int false）但 getTypeConverter(String,int) 仍合成有损转换器 MethodHandle(String)int；宽化/下转转换器均非 null 且可调用（invokeWithArguments("123")==123）。
- asType 返回值窄化；(int) 目标触发 asTypeLosslessReturn 回退 handle 返回类型。
- compareConversion 默认规则（方法调用转换可者胜、双可 INDETERMINATE）。
- getGuardedInvocation 直连真实重链；filterInternalObjects 默认恒等；getWithLookup 求值 supplier。
