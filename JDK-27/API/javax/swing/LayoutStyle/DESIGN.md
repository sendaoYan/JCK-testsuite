# javax.swing.LayoutStyle 测试设计

## 1. API 功能
组件间/组件到容器边距的平台无关抽象：getInstance/setInstance 管共享实例，
getPreferredGap（RELATED/UNRELATED/INDENT 三档 × 四方向）、getContainerGap 交由当前 LaF 的
LayoutStyle 实现给出像素值。

## 2. Java 用户如何使用
自定义间距规范时 `LayoutStyle.setInstance(myStyle)`；GroupLayout 的 autoCreateGaps 内部经
GroupLayout.setLayoutStyle 委托到它，FormBuilder 类工具直接查询。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/LayoutStyle.java`（setInstance(null) 复位逻辑）；
MetalLookAndFeel 相关 spacing 定义；GroupLayout.java 的用法。

## 4. 测试用例设计 + 易错细节
- ComponentPlacement 枚举恰 3 个值且 RELATED=0/UNRELATED=1/INDENT=2（ordinal 锁定）。
- getInstance 幂等（同一实例）；**setInstance(null) 恢复到之前的共享实例（不是 null）**，
  自定义实例随即失联——复位语义必须锁定。
- 匿名子类覆写 getPreferredGap=7/getContainerGap=3，安装后 getInstance()==custom，
  两个覆写方法直接可调（返回覆写值而非 LaF 值）。
- Metal 下逐值锁定：RELATED 四方向全 6px；UNRELATED/INDENT 12px；getContainerGap 12px
  （这些值来自 Metal 的按钮/标签默认间距，依赖先 setLookAndFeel(Metal)）。
- 抽象类：new LayoutStyle(){...} 匿名实现合法，两抽象方法签名（JComponent,JComponent,
  ComponentPlacement,int,Container)/(JComponent,int,Container)。

自动化实现位于同目录 `*Test.java`。
