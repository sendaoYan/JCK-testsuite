# jdk.dynalink.NamedOperation 设计案

## 1. API 功能
命名操作：baseOperation + name。

## 2. Java 用户如何使用
new NamedOperation(StandardOperation.GET,"x") + getBaseOperation/getName/changeName。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/NamedOperation.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+改名+静态访问+withNamespace 包装。
## 5. 契约深化（JDK-27）
- name 是任意 Object（枚举/Integer 保真，toString "GET:42"）。
- equals/hashCode 遵循 base+name；changeName 不可变（原对象不动）；静态 getBaseOperation/getName 对裸操作返回原操作/null。
- 构造校验：null base→NPE、NamedOperation base→IAE、null name→NPE；named 再 named→IAE。
