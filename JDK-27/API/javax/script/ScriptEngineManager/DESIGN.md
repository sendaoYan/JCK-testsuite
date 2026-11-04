# javax.script.ScriptEngineManager 测试设计

## 1. API 功能
脚本引擎管理器：发现引擎、按名/扩展/MIME 查找、全局绑定、显式注册。

按方法分组：
- 构造: (), (ClassLoader)
- 绑定: getBindings, setBindings, put, get
- 查找: getEngineByName/ByExtension/ByMimeType
- 注册: registerEngineName/Extension/MimeType
- 发现: getEngineFactories

## 2. Java 用户如何使用
用户用 manager 获取引擎执行脚本。测试用自定义工厂注册名/扩展/MIME 后解析，并验证全局绑定与 NPE 校验。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/ScriptEngineManager.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java, ProviderTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、getEngineByName 等与 registerEngineName
- 黑盒: put/get；set/getBindings；未知引擎 null；注册名/扩展/MIME 解析；register 空参 NPE

自动化实现位于同目录 `ScriptEngineManagerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
