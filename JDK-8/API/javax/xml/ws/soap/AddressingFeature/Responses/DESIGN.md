# javax.xml.ws.soap.AddressingFeature.Responses — 设计文档

## 一、API 功能
`Responses` 是枚举（final），恰 3 个常量：`ANONYMOUS`（仅匿名回复端点）、`NON_ANONYMOUS`（仅非匿名）、`ALL`（两者皆可）。作为 AddressingFeature 第 4 构造器参数/ @Addressing.responses 的取值，约束 WS-Addressing 回复端点形态。

## 二、使用方式
`new AddressingFeature(true, false, Responses.ANONYMOUS)` 或 `@Addressing(responses = Responses.NON_ANONYMOUS)`。测试验证枚举面（次序/ordinal/name/valueOf）、负例 valueOf、与 feature 构造的集成回读。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.AddressingFeature.Responses
- JSR 224 §9.6.1；WS-Addressing 2005/08
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/AddressingFeature/Responses/ResponsesTest.java

## 四、测试用例设计 + 易错规范细节
- values() 恰 3 且次序 [ANONYMOUS, NON_ANONYMOUS, ALL]；ordinal 0/1/2；toString=name。
- valueOf 大小写敏感：小写全名 → IAE；"NEVER"/""/null → IAE（null → NPE）。
- 合成字段 $VALUES 须过滤 isSynthetic()（真实字段恰 3）。
- 集成：new AddressingFeature(b,r,Responses.X).getResponses()==X 对 3 值逐一验证；默认（无参构造）为 ALL。
- 易错点：枚举不可 clone/final 类；ordinal 与声明序一致是规范保证，可断言。
