# jdk.dynalink.beans.MissingMemberHandlerFactory 设计案

## 1. API 功能
缺失成员处理工厂接口。

## 2. Java 用户如何使用
由 BeansLinker 提供。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/beans/MissingMemberHandlerFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 函数式接口单方法（createMissingMemberHandler(LinkRequest, LinkerServices)）+ 包位置 jdk.dynalink.beans。
- 真实闭环：BeansLinker(factory) 下 GET 缺失属性返回 handler 值；handler 收到缺失名；handler 返回 null → 回落 NSDME；已有成员不受影响。
- handler 约束（源码断言）：返回 handle 的类型须与 site 类型一致（仅返回类型可不同）。
