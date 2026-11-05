# jdk.dynalink.beans.StaticClass 设计案

## 1. API 功能
静态类包装：forClass + getRepresentedClass。

## 2. Java 用户如何使用
StaticClass.forClass(String.class)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/beans/StaticClass.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 forClass+equals+hashCode+toString。
## 5. 契约深化（JDK-27）
- forClass 每类缓存同实例；toString "StaticClass[<FQN>]"；equals 恒等语义；数组/接口类可包装。
- 静态成员闭环：GET:PROPERTY Integer.MAX_VALUE；GET:METHOD System.currentTimeMillis → CALL 时须把 StaticClass 作为第二实参传递（静态动态方法保留 StaticClass 形参）；NEW on StaticClass for Object。
- Class 接收者（ClassLinker）GET:PROPERTY:name 解析所表示类的静态属性。
