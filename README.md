### char-configservice

[![Build](https://github.com/CharLemAznable/char-configservice/actions/workflows/build.yml/badge.svg)](https://github.com/CharLemAznable/char-configservice/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/char-configservice/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/char-configservice/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)
![GitHub code size](https://img.shields.io/github/languages/code-size/CharLemAznable/char-configservice)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=alert_status)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=bugs)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=security_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=sqale_index)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=code_smells)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=ncloc)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=coverage)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-configservice&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-configservice)

配置服务客户端, 定义接口读取apollo/diamond配置.

##### Maven Dependency

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>char-configservice</artifactId>
  <version>2022.0.3</version>
</dependency>
```

##### Maven Dependency SNAPSHOT

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>char-configservice</artifactId>
  <version>2022.0.4-SNAPSHOT</version>
</dependency>
```

#### 1. 快速开始

##### 1.1. 读取apollo配置

**1.1.1. 简单配置**

```
# namespace:XXX propertyName:YYY
ZZZ
```

```java
@ApolloConfig(namespace = "XXX")
public interface MyConfig {
    @ApolloConfig("YYY")
    String value();
}

MyConfig myConfig = ApolloFactory.getApollo(MyConfig.class);
String value = myConfig.value(); // value: "ZZZ"
```

**1.1.2. Properties配置**

```
# namespace:XXX propertyName:YYY
key1=value1
key2=value2
```

```java
@ApolloConfig(namespace = "XXX")
public interface MyConfig {
    @ApolloConfig("YYY")
    Properties value();
}

MyConfig myConfig = ApolloFactory.getApollo(MyConfig.class);
Properties value = myConfig.value();
String value1 = value.getProperty("key1"); // value1: "value1"
String value2 = value.getProperty("key2"); // value2: "value2"
```

**1.1.3. Properties配置按键读取**

```
# namespace:XXX propertyName:YYY
key1=value1
key2=value2
```

```java
@ApolloConfig(namespace = "XXX", propertyName = "YYY")
public interface MyConfig {
    @ApolloConfig("key1")
    String key1();
    @ApolloConfig("key2")
    String key2();
}

MyConfig myConfig = ApolloFactory.getApollo(MyConfig.class);
String value1 = myConfig.key1(); // value1: "value1"
String value2 = myConfig.key2(); // value2: "value2"
```

##### 1.2. 读取diamond配置

与apollo配置类似, diamond配置的```group:dataId```对应apollo配置的```namespace:propertyName```.

将java代码中的```@ApolloConfig```替换为```@DiamondConfig```, 将```ApolloFactory.getApollo()```替换为```DiamondFactory.getDiamond()```即可.

#### 2. 配置坐标的指定

编写配置客户端接口时, 需要使用```@ApolloConfig```/```@DiamondConfig```注解, 在接口上标识其为配置客户端接口, 在接口或方法上指定配置读取的坐标.
* 当注解在接口上时, ```namespace```/```group```的默认值为```"application"```/```"DEFAULT_GROUP"```, ```propertyName```/```dataId```/```value```的默认值为```""```.
* 当注解在方法上时, 上述注解属性的默认值都是```""```.

##### 2.1. 默认坐标

```java
@ApolloConfig
@DiamondConfig
public interface MyConfig {
    @ApolloConfig // 可省略
    @DiamondConfig // 可省略
    String value();
}
```

读取配置:
```
# apollo
namespace: application
propertyName: value

# diamond
group: DEFAULT_GROUP
dataId: value

String value();方法返回配置完整内容字符串
```

##### 2.2. 仅指定接口的namespace/group

```java
@ApolloConfig(namespace = "XXX")
@DiamondConfig(group = "XXX")
public interface MyConfig {
    @ApolloConfig // 可省略
    @DiamondConfig // 可省略
    String value();
}
```

读取配置:
```
# apollo
namespace: XXX
propertyName: value

# diamond
group: XXX
dataId: value

String value();方法返回配置完整内容字符串
```

##### 2.3. 仅指定接口的propertyName/dataId

```java
@ApolloConfig(propertyName = "YYY")
@DiamondConfig(dataId = "YYY")
public interface MyConfig {
    @ApolloConfig // 可省略
    @DiamondConfig // 可省略
    String value();
}
```

读取配置:
```
# apollo
namespace: application
propertyName: YYY

# diamond
group: DEFAULT_GROUP
dataId: YYY

String value();方法返回配置内容按Properties解析后的键value的配置值
```

##### 2.4. 仅指定方法的namespace/group

```java
@ApolloConfig
@DiamondConfig
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ")
    @DiamondConfig(group = "ZZZ")
    String value();
}
```

读取配置:
```
# apollo
namespace: ZZZ
propertyName: value

# diamond
group: ZZZ
dataId: value

String value();方法返回配置完整内容字符串
```

##### 2.5. 仅指定方法的propertyName/dataId

```java
@ApolloConfig
@DiamondConfig
public interface MyConfig {
    @ApolloConfig(propertyName = "abc")
    @DiamondConfig(dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: application
propertyName: abc

# diamond
group: DEFAULT_GROUP
dataId: abc

String value();方法返回配置完整内容字符串
```

##### 2.6. 指定接口的namespace/group和propertyName/dataId

```java
@ApolloConfig(namespace = "XXX", propertyName = "YYY")
@DiamondConfig(group = "XXX", dataId = "YYY")
public interface MyConfig {
    @ApolloConfig // 可省略
    @DiamondConfig // 可省略
    String value();
}
```

读取配置:
```
# apollo
namespace: XXX
propertyName: YYY

# diamond
group: XXX
dataId: YYY

String value();方法返回配置内容按Properties解析后的键value的配置值
```

##### 2.7. 指定方法的namespace/group和propertyName/dataId

```java
@ApolloConfig
@DiamondConfig
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ", propertyName = "abc")
    @DiamondConfig(group = "ZZZ", dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: ZZZ
propertyName: abc

# diamond
group: ZZZ
dataId: abc

String value();方法返回配置完整内容字符串
```

##### 2.8. 指定接口和方法的namespace/group

```java
@ApolloConfig(namespace = "XXX")
@DiamondConfig(group = "XXX")
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ")
    @DiamondConfig(group = "ZZZ")
    String value();
}
```

读取配置:
```
# apollo
namespace: ZZZ
propertyName: value

# diamond
group: ZZZ
dataId: value

String value();方法返回配置完整内容字符串
```

##### 2.9. 指定接口和方法的propertyName/dataId

```java
@ApolloConfig(propertyName = "YYY")
@DiamondConfig(dataId = "YYY")
public interface MyConfig {
    @ApolloConfig(propertyName = "abc")
    @DiamondConfig(dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: application
propertyName: YYY

# diamond
group: DEFAULT_GROUP
dataId: YYY

String value();方法返回配置内容按Properties解析后的键abc的配置值
```

##### 2.10. 指定接口的namespace/group, 并指定方法的propertyName/dataId

```java
@ApolloConfig(namespace = "XXX")
@DiamondConfig(group = "XXX")
public interface MyConfig {
    @ApolloConfig(propertyName = "abc")
    @DiamondConfig(dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: XXX
propertyName: abc

# diamond
group: XXX
dataId: abc

String value();方法返回配置完整内容字符串
```

##### 2.11. 指定接口的propertyName/dataId, 指定方法的namespace/group

```java
@ApolloConfig(propertyName = "YYY")
@DiamondConfig(dataId = "YYY")
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ")
    @DiamondConfig(group = "ZZZ")
    String value();
}
```

读取配置:
```
# apollo
namespace: application
propertyName: YYY

# diamond
group: DEFAULT_GROUP
dataId: YYY

String value();方法返回配置内容按Properties解析后的键ZZZ.value的配置值
```

##### 2.12. 不指定接口的namespace/group

```java
@ApolloConfig(propertyName = "YYY")
@DiamondConfig(dataId = "YYY")
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ", propertyName = "abc")
    @DiamondConfig(group = "ZZZ", dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: application
propertyName: YYY

# diamond
group: DEFAULT_GROUP
dataId: YYY

String value();方法返回配置内容按Properties解析后的键ZZZ.abc的配置值
```

##### 2.13. 不指定接口的propertyName/dataId

```java
@ApolloConfig(namespace = "XXX")
@DiamondConfig(group = "XXX")
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ", propertyName = "abc")
    @DiamondConfig(group = "ZZZ", dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: ZZZ
propertyName: abc

# diamond
group: ZZZ
dataId: abc

String value();方法返回配置完整内容字符串
```

##### 2.14. 不指定方法的namespace/group

```java
@ApolloConfig(namespace = "XXX", propertyName = "YYY")
@DiamondConfig(group = "XXX", dataId = "YYY")
public interface MyConfig {
    @ApolloConfig(propertyName = "abc")
    @DiamondConfig(dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: XXX
propertyName: YYY

# diamond
group: XXX
dataId: YYY

String value();方法返回配置内容按Properties解析后的键abc的配置值
```

##### 2.15. 不指定方法的propertyName/dataId

```java
@ApolloConfig(namespace = "XXX", propertyName = "YYY")
@DiamondConfig(group = "XXX", dataId = "YYY")
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ")
    @DiamondConfig(group = "ZZZ")
    String value();
}
```

读取配置:
```
# apollo
namespace: XXX
propertyName: YYY

# diamond
group: XXX
dataId: YYY

String value();方法返回配置内容按Properties解析后的键ZZZ.value的配置值
```

##### 2.16. 全部指定坐标

```java
@ApolloConfig(namespace = "XXX", propertyName = "YYY")
@DiamondConfig(group = "XXX", dataId = "YYY")
public interface MyConfig {
    @ApolloConfig(namespace = "ZZZ", propertyName = "abc")
    @DiamondConfig(group = "ZZZ", dataId = "abc")
    String value();
}
```

读取配置:
```
# apollo
namespace: XXX
propertyName: YYY

# diamond
group: XXX
dataId: YYY

String value();方法返回配置内容按Properties解析后的键ZZZ.abc的配置值
```

#### 3. 指定坐标时使用环境变量

指定坐标值时, 可使用```${key}```标识环境变量.

环境变量源为类路径中的```configservice.env.props```配置文件和```Arguments```启动参数.

其中```Arguments```变量的优先级高于配置文件.

#### 4. 类型转换

默认根据方法返回类型解析读取到的配置值, 并做类型转换.
* 基本类型和字符串会直接转换并返回
* Map及其子类型, 将返回Properties类型对象
* 非集合类型的泛型类型, 将使用```DiamondUtils.parseObject()```方法进行解析
* 集合类型将使用```DiamondUtils.parseObjects()```方法进行解析
* 可在方法上添加```@ConfigValueParse```注解, 进行自定义解析

#### 5. 配置默认值

当未读取到指定坐标的配置值时, 可使用配置默认值返回缺省配置值.

可使用注解中的```defaultValue```属性, 或方法调用的第一个入参配置默认值.

方法入参的优先级高于```defaultValue```属性.

```defaultValue```属性配置的默认值也可使用使用环境变量, 也会进行类型转换.

可在方法上添加```@DefaultEmptyValue```注解, 配置默认值为```""```而非```null```.

#### 6. 配置读取并缓存

可使用注解中的```cacheSeconds```属性, 指定配置读取到内存后的缓存时长(单位: 秒).

#### 7. 通用配置读取

配置客户端接口继承```ConfigGetter```接口, 即可使用通用方法按键读取配置值.

#### 8. 支持TOML格式

当配置内容以```# toml```开头时, 将按TOML格式读取配置为Properties对象.

#### 9. 兼容使用apollo/diamond

配置客户端接口同时添加```@ApolloConfig```和```@DiamondConfig```注解, 或添加```@Config```注解.

使用```ConfigFactory.getConfig()```方法获取客户端实例.

当类路径中仅包含apollo-client或diamond-client时, 将自动选取对应的配置实现.

当类路径中同时包含apollo-client和diamond-client时, 根据[环境变量](#3-指定坐标时使用环境变量)中设置的```ConfigService```值选取对应的配置实现.

```
# configservice.env.props
ConfigService=apollo

# Arguments
--ConfigService=apollo
```
默认使用diamond配置实现.

#### 10. 在Spring中使用

使用```@ApolloScan```/```@DiamondScan```/```@ConfigScan```指定扫描加载包路径.

包路径中所有添加```@ApolloConfig```/```@DiamondConfig```/```@Config```注解的接口都将生成对应的配置客户端实例并注入SpringContext.

#### 11. 在Guice中使用

使用```ApolloModular```/```DiamondModular```/```ConfigModular```按类或包路径扫描加载.

创建的```Module```中将包含对应的配置客户端实例.
