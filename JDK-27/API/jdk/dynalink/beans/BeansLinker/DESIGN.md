# jdk.dynalink.beans.BeansLinker 设计案

## 1. API 功能
Bean 链接器：属性/方法名枚举 + getGuardedInvocation。

## 2. Java 用户如何使用
new BeansLinker() + getInstanceMethodNames 等静态方法。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/beans/BeansLinker.java；test/jdk/jdk/dynalink/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒属性/方法名枚举+isDynamicMethod。
## 5. 契约深化（JDK-27）
- 成员名集合精确表：POJO 可读属性恰 {class, field, name}、可写 {field}；System 静态 out/err/currentTimeMillis；Integer.MAX_VALUE。
- getConstructorMethod 真实签名格式 "int, int" → 非空 isDynamicMethod+isDynamicConstructor；未知签名/无公共构造类/原始类→null。
- getLinkerForClass：静态 ClassValue 跨实例共享同 linker；null→NPE；返回 TypeBased。
- 全操作真实闭环：GET/SET:PROPERTY（值与副作用断言）、GET:ELEMENT（List/Map）、REMOVE:ELEMENT（List 移除）、GET:METHOD+CALL 两段式（SimpleDynamicMethod 恒等 + "name-41"）、NEW on StaticClass。
