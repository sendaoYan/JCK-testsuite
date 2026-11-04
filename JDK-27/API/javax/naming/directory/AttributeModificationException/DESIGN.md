# javax.naming.directory.AttributeModificationException 设计案

## 1. API 功能
异常类（`extends javax.naming.NamingException`）。
- 构造器：`AttributeModificationException(String)`、`AttributeModificationException()`。
- 专有方法：`setUnexecutedModifications(ModificationItem[])`、`getUnexecutedModifications()`、`toString()`。
- 继承：`getMessage()`/`getExplanation()` 等。

## 2. Java 用户如何使用
目录 `modifyAttributes` 部分失败时抛出；用户读取 `getUnexecutedModifications()` 获取未执行的修改项数组，据此重试或上报。默认未执行数组为 null。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/AttributeModificationException.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` 目录修改用例在异常路径检查未执行修改。

## 4. 测试用例设计
白盒（Test.testWhiteBox）：public/非接口/extends NamingException + 两个构造器 + 专有方法。
黑盒（Test.testBlackBox）：带消息构造；`setUnexecutedModifications` 数组往返（长度/操作码/属性 id）；无参构造消息为 null；toString 含消息。
