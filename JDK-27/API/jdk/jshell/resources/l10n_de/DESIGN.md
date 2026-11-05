# jdk.jshell.resources.l10n_de（德语翻译目录） 设计案

## 1. API 功能
l10n 的德语 ListResourceBundle 翻译。

## 2. Java 用户如何使用
Locale 机制按需加载。

## 3. 开源代码实际用法调研
来源：JDK-27 jimage 提取的 l10n_de.properties。

## 4. 测试用例设计 + 易错点
超类是 java.util.ListResourceBundle（不是 l10n——历史实现）；键集与基类完全一致；9 条消息全部本地化（与英文不同且非空）；抽样精确断言（"Snippet darf nicht Null sein"）；占位符结构与英文模板一致。

