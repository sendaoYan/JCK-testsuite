# jdk.dynalink.linker.support.Guards 设计案

## 1. API 功能
守卫工厂：isNull/isNotNull/isOfClass/getClassGuard 等。

## 2. Java 用户如何使用
Guards.isNull() 等静态方法。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/Guards.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒多种守卫工厂返回非空。
## 5. 契约深化（JDK-27）
- isOfClass 精确类匹配（子类不匹配）+ null 类/类型 NPE；isInstance 含子类；isInstance(pos) 只查指定槽位（其他槽不查）。
- isArray 双正负例；isNull/isNotNull；getClassGuard/getInstanceOfGuard/getIdentityGuard（同对象/异对象）。
- asType 与 asType(LinkerServices, ...) 参数适配真实调用。
- 源码注意：参数类型即目标类时 isOfClass 会记录"恒真"告警（测试一律用 Object 参数类型规避）。
