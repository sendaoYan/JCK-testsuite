# javax.management.DefaultLoaderRepository 黑盒行为测试设计

## 1. API 功能
- `@Deprecated` 类。
- 静态方法：`loadClass(String)`、`loadClassWithout(ClassLoader, String)`。

## 2. Java 用户如何使用
兼容旧 JMX 的类加载入口：遍历所有被跟踪的 MBeanServer 的 ClassLoaderRepository 加载类，找不到抛 ClassNotFoundException。
```java
Class<?> c = DefaultLoaderRepository.loadClass("com.example.Foo");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/loading/SystemClassLoaderTest.java`、`ArrayClassTest.java`：类加载行为。
- 实现权威：`src/java.management/share/classes/javax/management/loading/DefaultLoaderRepository.java`（内部走 `MBeanServerFactory.findMBeanServer(null)`）。

## 4. 测试用例设计 + 自动化实现
- 黑盒：先 `MBeanServerFactory.createMBeanServer` 创建被跟踪服务器，loadClass 能解析 java.lang.String/ArrayList，loadClassWithout(null,...) 能解析 Integer；finally 释放服务器；缺失类抛 ClassNotFoundException。
- 白盒：public 类、@Deprecated、静态 loadClass/loadClassWithout。

## 深化（2026-11）：仓库语义闭环

### 新增覆盖（@Deprecated 根类，自身行为）
1. **零 tracked server**：loadClass/loadClassWithout → ClassNotFoundException（仓库只汇聚 findMBeanServer(null) 的 tracked server 的 ClassLoaderRepository）。
2. **untracked newMBeanServer 不贡献**：只建 untracked server 时依旧 CNFE（负例）。
3. **tracked server 存在时**：loadClass 解析 java.lang.String/javax.management.MBeanServer（类身份 ==）；loadClassWithout(null, name) 同效。
4. **release 后清空**：releaseMBeanServer 后再 loadClass 回到 CNFE。
5. **未知类**：CNFE 且消息含类名。
6. **结构**：两方法均 static；类 @Deprecated。

### 参考来源
- Oracle javadoc: DefaultLoaderRepository（deprecated 说明与查找顺序）。
