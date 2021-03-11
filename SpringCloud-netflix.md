# 微服务间通信 -- RestTemplate 

通过RestTemplate  封装了http请求，可以传递数据



# 组合注解

启用越来越多的组件，都需要在 启动类 添加注解，此时出现组合注解，通过一个代替多个

@RestController = @RequestBody + @Controller

​				返回json注解            控制器标识注解



@SpringCloudApplication  = @SpringBootApplication + @EnableDiscoveryClient + @EnableCircuitBreaker

​							springboot注解                     euerka服务发现注解             hystrix熔断器开启注解



## 生产者 --- 查询数据库，获得信息

配置信息

```properties
#tomcat端口号
server.port=8088
#访问时项目名，不写默认为 / (没有)
server.servlet.context-path=/


#配置数据源
#spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:mysql://localhost:3306/springcloud?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

#注册mapper文件，目录指定为mappers目录下.xml结尾
mybatis.mapper-locations=classpath:mappers/*.xml
#简化实体类的全类名
mybatis.type-aliases-package=com.xyc.entity
```



```java
package com.xyc.controller;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest")
public class RestController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getOne(@PathVariable("id") Integer id){
        User one = userService.getOne(id);
        return one;
    }
}

```

消费者 --- 通过RestTemplate访问消费者，获得信息

```java
// 注入 RestTemplate
package com.xyc.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplatConfig {

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}

```

```java
package com.xyc.controller;

@RestController
@RequestMapping("/consumer")
public class TestController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/{id}")
    public User getOne(@PathVariable("id")Integer id){

        User user = restTemplate.getForObject("http://localhost:8088/rest/" + id, User.class);
        return user;
    }
}
```

访问   http://localhost:8080/consumer/1 可以获得信息【默认端口无配置更改】 --- 信息来自 http://localhost:8088/rest/1



# Eureka

注册中心，服务发现 [也只做服务注册]

如果服务越来越多，通过硬编码形式【如 ：restTemplate.getForObject("http://localhost:8088/rest/" + id, User.class);】

​									将服务的地址写入代码，维护不变，繁琐复杂  -----》 出现eureka提供服务的注册记录

![1615210810873](assets/1615210810873.png)

## 搭建

![1615261333787](assets/1615261333787.png)

```pom
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.5.RELEASE</version>
    </parent>

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.SR1</spring-cloud.version>
    </properties>

    <dependencies>

        <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>runtime</scope>
        </dependency>

		<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
            <version>2.1.4.RELEASE</version>
        </dependency>
		
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
```

```java
package com.xyc;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

//声明当前引用为eurka服务
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class);
    }
}
```

```yml
server:
  port: 10086
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url:
      # eureka服务地址，如果为集群，需指定其他地址
      defaultZone: http://127.0.0.1:10086/eureka
    #是否注册自己为服务  ---- 集群时需要设置为true
    register-with-eureka: false
    #不拉取服务--因为不消费
    fetch-registry: false
```

启动 访问 localhost:10086 即可访问 eureka提供的 web页面

## 服务注册与发现

将服务注册进入erueka，并可实现调用

### 服务注册 --- 服务提供者

在 提供服务的应用【生产者】 中添加 eureka - Client 依赖【自动将服务注册到EurekaServer的服务地址列表】

![1615266745007](assets/1615266745007.png)

```xml
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.1.4.RELEASE</version>
        </dependency>
```

```java
package com.xyc;
//声明 开启eureka客户端发现功能
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.xyc.dao")
public class RestTemplateApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestTemplateApplication.class);
    }
}
```

```properties
#tomcat端口号
server.port=8088
#访问时项目名，不写默认为 / (没有)
server.servlet.context-path=/

#配置该服务的名称 --- erueka Web界面将以改名字显示
spring.application.name=user-service
eureka.client.serviceUrl.defaultZone=http://localhost:10086/eureka/
#配置数据源
#spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:mysql://localhost:3306/springcloud?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

#注册mapper文件，目录指定为mappers目录下.xml结尾
mybatis.mapper-locations=classpath:mappers/*.xml
#简化实体类的全类名
mybatis.type-aliases-package=com.xyc.entity

```



### 服务发现 --- 服务消费者

在 消费服务的应用 【消费者】中添加 eureka - Client 依赖，之后使用工具类，根据服务名，获取服务地址列表

![1615266786489](assets/1615266786489.png)

```xml
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.1.4.RELEASE</version>
        </dependency>
```

```java
package com.xyc;

@EnableDiscoveryClient //开启eureka客户端发现功能
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}

```

```yml
spring:
  application:
    name: user-consumer
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
```

```java
package com.xyc.controller;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import java.util.List;

@RestController
@RequestMapping("/consumer")
public class TestController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;
    @GetMapping("/{id}")
    public User getOne(@PathVariable("id") Integer id) {
        //原写死url -- 不可
//        String url = "http://localhost:8088/rest/" + id;
        // 通过discoveryClient获得服务地址 --- 通过服务名
        List<ServiceInstance> serviceList = discoveryClient.getInstances("user-service");
        ServiceInstance serviceInstance = serviceList.get(0);
        String url = "http://"+serviceInstance.getHost()+":"+serviceInstance.getPort()+"/rest/"+id;
        System.out.println("url = " + url);
        User user = restTemplate.getForObject(url, User.class);
        return user;
    }
}
```

## 高可用

![1615269623764](assets/1615269623764.png)

![1615269687029](assets/1615269687029.png)

![1615269723436](assets/1615269723436.png)

${port:10086} 如果前面的port有值，就是用前面的port，否则使用10086

![1615269788977](assets/1615269788977.png)



### 搭建eureka-Server02

将 eureka-Server01 设置进来

```yml
server:
  port: 10087
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url:
      # eureka服务地址，如果为集群，需指定其他地址  --- 向另一个EurekaServer注册自己
      defaultZone: http://127.0.0.1:10086/eureka
    #是否注册自己为服务
    register-with-eureka: true
    #拉取服务
    fetch-registry: true
```

```java
package com.xyc;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

//声明当前引用为eurka服务
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class);
    }
}
```

### 修改eureka-Server01 服务配置文件

向 eureka-Server02 注册自己

```yml
server:
  port: 10086
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url:
      # eureka服务地址，如果为集群，需指定其他地址
      defaultZone: http://127.0.0.1:10087/eureka
    #是否注册自己为服务
    register-with-eureka: true
    #不拉取服务--因为不消费
    fetch-registry: true
```

### 结果

eureka-Server01 和 eureka-Server02 都可以访问各自的web页面

注册向 eureka-Server01 的 ==服务会同步==到 eureka-Server02【最好让服务同时向各个erueka进行注册，保证高可用】

## Eureka客户端和服务端配置

![1615274198698](assets/1615274198698.png)

### 客户端设置

#### 服务提供者

##### 服务注册与续约

![1615274307908](assets/1615274307908.png)

![1615274653391](assets/1615274653391.png)



```properties
#tomcat端口号
server.port=8088
#访问时项目名，不写默认为 / (没有)
server.servlet.context-path=/

#配置该服务的名称
spring.application.name=user-service
eureka.client.serviceUrl.defaultZone=http://localhost:10086/eureka/
#倾向使用IP访问，而不是HOst【比如不会再是 loaclhost】
eureka.instance.prefer-ip-address=true
#指定使用哪个IP
eureka.instance.ip-address=127.0.0.1
#服务失效，间隔90s【默认也是90s】--90s没有续约，就会被剔除
eureka.instance.lease-expiration-duration-in-seconds=90
#服务续约，间隔30s【默认也是30s】
eureka.instance.lease-renewal-interval-in-seconds=30

#配置数据源
#spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:mysql://localhost:3306/springcloud?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

#注册mapper文件，目录指定为mappers目录下.xml结尾
mybatis.mapper-locations=classpath:mappers/*.xml
#简化实体类的全类名
mybatis.type-aliases-package=com.xyc.entity

```

#### 服务消费者

![1615274907863](assets/1615274907863.png)

```yml
spring:
  application:
    name: user-consumer
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    #每个30s拉取一次服务列表【默认也是30s】
    registry-fetch-interval-seconds: 30
```

#### EurekaServer

##### 服务下线与剔除

![1615275207866](../%E7%AC%94%E8%AE%B0/assets/1615275207866.png)

![1615275483136](assets/1615275483136.png)

```yml
server:
  port: 10086
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url:
      # eureka服务地址，如果为集群，需指定其他地址
      defaultZone: http://127.0.0.1:10087/eureka
    #是否注册自己为服务
    register-with-eureka: false
    #不拉取服务--因为不消费
    fetch-registry: false
  server:
    #剔除已经失效服务的间隔时间 ms 【每60s会执行将已经90s没有续约的服务剔除】
    eviction-interval-timer-in-ms: 6000
    #是否打开自我保护【默认true】
    enable-self-preservation: true
```

# Ribbon 负载均衡

负载均衡是一个算法，实现从地址列表中选取一个地址进行服务调用

spring提供 ribbon 来进行负载均衡，ribbon默认提供了多种负载均衡算法【如 轮询、随机等】，也可自定义算法

Eureka已经集成了 ribbon， 简单修改代码即可使用

### 使用

2个服务提供者，1个消费者，让消费者通过 参数 id 的不同来访问不同的服务提供者

![1615276044557](assets/1615276044557.png)

![1615276088461](assets/1615276088461.png)

#### 快速开启2个生产者

##### 修改端口可变

```java
#tomcat端口号 --- 如果启动时设置其他端口则使用其他，否则使用8088
#可以在启动时添加参数 -Dport=8089 设置使用其他端口
server.port=${port:8088}
#访问时项目名，不写默认为 / (没有)
server.servlet.context-path=/

#配置该服务的名称
spring.application.name=user-service
eureka.client.serviceUrl.defaultZone=http://localhost:10086/eureka/
#倾向使用IP访问，而不是HOst【比如不会再是 loaclhost】
eureka.instance.prefer-ip-address=true
#指定使用哪个IP
eureka.instance.ip-address=127.0.0.1
#服务失效，间隔90s【默认也是90s】--90s没有续约，就会被剔除
eureka.instance.lease-expiration-duration-in-seconds=90
#服务续约，间隔30s【默认也是30s】
eureka.instance.lease-renewal-interval-in-seconds=30

#配置数据源
#spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:mysql://localhost:3306/springcloud?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

#注册mapper文件，目录指定为mappers目录下.xml结尾
mybatis.mapper-locations=classpath:mappers/*.xml
#简化实体类的全类名
mybatis.type-aliases-package=com.xyc.entity

```

添加启动时参数【注意使用不同启动器】

![1615276640747](assets/1615276640747.png)

#### 修改消费者

修该 RestTemplate 的Bean ---  添加注解 @loadBanlanced

```java
package com.xyc.config;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
@Configuration
public class RestTemplatConfig {
    @Bean
    @LoadBalanced   //默认使采用轮询的方式
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

##### 修改负载均衡策略【非必要】

![1615277975685](assets/1615277975685.png)

修改controller ---- 使通过  http://注册的服务名 / 请求的控制器 / 参数 -- 的形式实现访问

```java
package com.xyc.controller;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/consumer")
public class TestController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;
    @GetMapping("/{id}")
    public User getOne(@PathVariable("id") Integer id) {
        String url = "http://user-service/rest/"+id;
        System.out.println("url = " + url);
        User user = restTemplate.getForObject(url, User.class);
        return user;
    }
}
```

#### 测试

发送请求 ： http://localhost:8080/consumer/1 访问消费者

​					消费者通过 http://user-service/rest/"+id  实现了查询

#### 简单源码分析

 1  ribbon通过拦截器实现了负载均衡

 2  ribbon的jar已经集成在了 eureka 的jar中

 3 通过commons设置启用

![1615277822265](assets/1615277822265.png)

![1615277898758](assets/1615277898758.png)

# Hystrix 熔断器

netflix 开源的一个延迟和容错库，，用于隔离远程访问服务、第三方库，防止出现级联失败

##### 雪崩问题

一个请求可能会调用多个服务，但是其中某个出现问题，请求就会持续等待，tomcat也不会释放该请求，

​		当越来越多请求过来，导致越来越多的线程阻塞，最终服务器资源耗尽，导致其他服务也不再可用，造成雪崩效用

##### 解决方式：

​	==服务降级==

​	==服务熔断==

## 线程隔离&服务降级

线程隔离： 用户请求不直接访问服务，而是使用线程池中空闲的线程访问服务，加速失败判断时间

​					每个服务都有自己的线程池，一个请求访问多个服务都采用其各自的线程池中空闲的线程；

​					当某个线程池耗尽，直接导致该服务不可用，让请求即使知道结果【服务可不可用】，以作其他处理

服务降级： 及时返回服务调用失败的结果，让线程不因为等待服务而阻塞



![1615278867768](assets/1615278867768.png)

![1615278902958](assets/1615278902958.png)

### 服务降级使用

消费者

#### Hystrix依赖

```pom
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
```

#### 开启熔断

启动类添加

```java
/*
@EnableDiscoveryClient //开启eureka客户端发现功能
@SpringBootApplication
@EnableCircuitBreaker //开启熔断
*/
@SpringCloudApplication   //组合注解 可代替上面3个注解
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

#### 编写默认处理方法【降级逻辑】

用于服务无法访问时，调用，加速返回处理失败的结果

==降级的等待超时时间==默认是 1s ； 即如果1s没有相应结果，就会取执行降级处理

##### 针对某一方法降级

![1615280529261](assets/1615280529261.png)

##### 针对整个类降级

![1615281019269](assets/1615281019269.png)

##### 代码

```java
package com.xyc.controller;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;

@RestController
@RequestMapping("/consumer")
@Slf4j  //lombok注解
        //用于省略private  final Logger logger = LoggerFactory.getLogger(当前类名.class);
@DefaultProperties(defaultFallback = "defauleFallBackMethod")
public class TestController {

    public String defauleFallBackMethod(){
        return "可对整个类的方法进行降级处理。。。。";
    }

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/{id}")
    //设置服务无法访问时，采用myFallBack方法快速返回失败结果
    @HystrixCommand(fallbackMethod = "myFallBack")
    public String getOne(@PathVariable("id") Integer id) {
        String url = "http://user-service/rest/"+id;
        System.out.println("url = " + url);
        return restTemplate.getForObject(url, String.class);
    }

    public String myFallBack(Integer id){
        log.error("id查询失败。。。。。id:{}",id);
        return "网络紧急升级。。";
    }
}
```

#### 配置等待超时时间

单位 ms

![1615281293892](assets/1615281293892.png)

## 服务熔断

![1615281528210](assets/1615281528210.png)

![1615281742886](assets/1615281742886.png)

但多次短时间【20次，50%】都访问失败，断路器开启，就会进行全部服务降级，

等待5s后，断路器半开，再次尝试访问，如果成功，就会关闭断路器，

​																		依旧失败将再次开启断路器

##### 配置熔断策略

![1615282164354](assets/1615282164354.png)

![1615282216499](assets/1615282216499.png)

# Feign 对Ribbon优化

![1615282369625](assets/1615282369625.png)

![1615282406425](assets/1615282406425.png)

自动根据参数拼接 http 请求地址

## 使用Feign

![1615282476634](assets/1615282476634.png)

```pom
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
```

使用 @EnableFeignClient 开启feign功能

```java
package com.xyc;

//@EnableDiscoveryClient //开启eureka客户端发现功能
//@SpringBootApplication
//@EnableCircuitBreaker //开启熔断

@SpringCloudApplication //可代替上面3个注解
@EnableFeignClients //开启feign功能
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

编写客户端【一个接口】

![1615283676332](assets/1615283676332.png)

ComsumerFeignController.java  

```java
package com.xyc.controller;
import com.xyc.client.UserClientFeign;

@RestController
@RequestMapping("/cf")
public class ConsumerFeignController {
    @Autowired
    private UserClientFeign userClientFeign;
    @GetMapping("/{id}")
    public String getOne(@PathVariable Integer id){
        return userClientFeign.selectOne(id);
    }
}

```

访问  http://localhost:8080/cf/1   通过feign自动指向  http://user-service/rest/1  实现了查询功能

# Hystrix+Feign

配置 Feign 内置 的 ribbon配置项 和  hystrix熔断 fallback配置，即使用feign直接完成ribbon+hystrx

## ==注意！！==

并不用太在意 feign 对 Hystrix 的整合配置，

​	一方面，feign中的 hystrix 并不算好用

​	另一方面，可以直接使用hystrix的功能 + feign的负载和http拼接 ；不用强行使用feign中的hystrix



![1615284120508](assets/1615284120508.png)

## 负载均衡

![1615284145399](assets/1615284145399.png)

![1615284170969](assets/1615284170969.png)

==配置仅会对采用了FeignClient的controller发生作用==

## Hystrix支持【了解】

feign 已集成 hystrix ，只不过==默认关闭，需要设置开启==

![1615284407522](assets/1615284407522.png)

![1615284632768](assets/1615284632768.png)

==配置仅会对采用了FeignClient的controller发生作用==

## 请求压缩【了解】

![1615291440913](assets/1615291440913.png)

## 日志级别

![1615291756765](assets/1615291756765.png)

![1615291815301](assets/1615291815301.png)

![1615291793513](assets/1615291793513.png)

![1615291851822](assets/1615291851822.png)

![1615291874250](assets/1615291874250.png)

![1615291940462](assets/1615291940462.png)

# Spring Cloud GateWay网关

![1615352663709](assets/1615352663709.png)

spring gateway是对zuul的一种替代实现方案【因为zuul不在维护更新】

基于Filter链的方式提供网关基本功能： 安全、监控\埋点、限流等

​	为微服务架构提供简单、有效、同意的API路由管理

![1615342190483](assets/1615342190483.png)

网关的核心功能 ： ==过滤和路由==

​	路由：

​	过滤：

## 引入gateway后的架构

![1615342287071](assets/1615342287071.png)

![1615352566870](assets/1615352566870.png)

## quickStart

![1615352874157](assets/1615352874157.png)

```pom
<dependencies>
        <!--本身既是服务，需要注册-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-netflix-eureka-client</artifactId>
            <version>2.1.4.RELEASE</version>
        </dependency>
        <!--gateway-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
            <version>2.1.1.RELEASE</version>
        </dependency>
    </dependencies>
```

```java
package com.xyc;

@SpringBootApplication
@EnableDiscoveryClient   //开启eureka client发现
public class GateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GateWayApplication.class);
    }
}
```

```yml
server:
  port: 10010
spring:
  application:
    name: gateway

  cloud:
    gateway:
      routes:
        #路由id，可任意
        - id: user-service-rout
          #代理的服务地址
          uri: http://127.0.0.1:8080
          #路由断言,可以匹配映射地址  ---
          #当访问路径包含 ip:10010/consumer/** 就会将访问转到 --> uri/consumer/**
          predicates:
            - Path=/consumer/**,/cf/**

eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka

```

### 测试 

访问 gateway   http://127.0.0.1:10010/consumer/1

​	匹配断言 /consumer/**  -----> 路径变为 : http://127.0.0.1:8080/consumer/1

## 使用服务名作为地址

 http:// ==127.0.0.1:10010/ consumer/1== 将路径写死了 ，

改变   ---》  使用 在 eureka中注册的服务名 进行访问



![1615366241388](assets/1615366241388.png)

![1615370321179](assets/1615370321179.png)

## 路由地址处理

客户端的请求地址和请求服务的地址出现偏差时，可以使用过滤器进行处理

### 前缀处理

如果 提供服务的 地址为 ：  http://127.0.0.1:8080/consumer/2

#### 添加前缀

访问是： http://127.0.0.1:8080 / 2    目的是 http://127.0.0.1:8080/ ==consumer== /2

需要添加前缀

![1615370883415](assets/1615370883415.png)

![1615370902045](assets/1615370902045.png)

#### 去除前缀

访问是： http://127.0.0.1:8080/ api /consumer/2   目的是 http://127.0.0.1:8080/ consumer/2

需要去除前缀

![1615370969228](assets/1615370969228.png)

## 过滤器

例：添加响应头信息Filter

![1615371601081](assets/1615371601081.png)



![1615371834165](assets/1615371834165.png)

## 自定义局部过滤器

配置自定义过滤器

![1615372737695](assets/1615372737695.png)

自定义过滤器

![1615372605987](assets/1615372605987.png)

## 自定义全局过滤器

全局过滤器直接注入spring使用

由于验证token是否存在

![1615372917668](assets/1615372917668.png)



## gateway配置负载和熔断【了解】

![1615373183916](assets/1615373183916.png)

## 跨域请求

![1615373361098](assets/1615373361098.png)

![1615373342234](assets/1615373342234.png)

## gateway集群和与feign区别

![1615373499530](assets/1615373499530.png)

# Spring Cloud Config配置中心

## 使用git管理，本地获取

![1615373672098](assets/1615373672098.png)

### 1 在gitee、或github上写好配置文件

### 2 创建配置中心服务

引入依赖, 、 创建启动类

![1615373988993](assets/1615373988993.png)

![1615374027494](assets/1615374027494.png)

![1615374135453](assets/1615374135453.png)

访问 ：  ip:12000/配置文件名 

### 3 微服务获取远程配置

修改微服务的配置文件获取方式，改为远程仓库拉取

#### 1 原工程增添依赖

![1615374349341](assets/1615374349341.png)

![1615374392209](assets/1615374392209.png)

####  2 删除原本配置文件

![1615374741271](assets/1615374741271.png)

#### 3 创建获取配置文件的配置文件

![1615374497956](assets/1615374497956.png)

![1615374540174](assets/1615374540174.png)



# 总结

![1615374895749](assets/1615374895749.png)