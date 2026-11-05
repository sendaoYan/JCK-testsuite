# jdk.dynalink.CallSiteDescriptor 设计案

## 1. API 功能
调用点描述符：operation + methodType + change*。

## 2. Java 用户如何使用
new CallSiteDescriptor(lookup, op, mt); getOperation/getMethodType/changeOperation/changeMethodType。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/CallSiteDescriptor.java；test/jdk/jdk/dynalink/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+改 operation/methodType+equals/hashCode/toString。
## 5. 契约深化（JDK-27）
- 构造器三个参数的 NPE 表。
- equals/hashCode 由 lookup(类+模式)+operation+methodType 决定：publicLookup 与 lookup 不等。
- toString 精确格式 "GET(Object)Object@<lookup>"；publicLookup 显示 "java.lang.Object/publicLookup"。
- changeOperation/changeMethodType 对原对象不可变、保另一分量；getLookup（继承自 SecureLookupSupplier）返回构造 lookup。
