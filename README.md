# Hexagonal Architecture with Maven for a Spring Boot microservice

## Motivation

I couldn't find on the internet any good project structure that expresses the hexagonal architecture and its constraints for a Spring Boot microservice. So I decided to create this sample project with my insights.

## Cardinality "Always One"

**One** *bounded context* to **one** *git repo* to **one** *maven project (with their modules)* to **one** *microservice* deployed as **one** *spring boot fat jar*.

## Structure and Dependencies

* thesystem (parent pom.xml)
  * api
  * app
  * domain
  * infra

```
api --> app --> domain <-- infra
 ^       ^        ^
 |_______|________|__________|
```


## thesystem

A parent pom project with common libraries shared between the 4 maven modules. There isn't a source folder here.

## api

This maven module belongs to the **infra layer** (outside the hexagon) because this is an **input adapter**. Here lives the `@RestController`. This **must be placed** in a separated module (outside the *infra module*) due to one strong reason:

1. To protect this module against a **transitive dependency** to the *domain*. This constraint is very important to avoid your domain objects leaking outside the hexagon. I have reached this just using `maven exclusions`

``` xml
<artifactId>api</artifactId>
<dependencies>
    <dependency>
        <groupId>com.thesystem</groupId>
        <artifactId>app</artifactId>
        <version>1.0.0</version>
        <exclusions>
            <exclusion>
                <groupId>com.thesystem</groupId>
                <artifactId>domain</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
...
</dependencies>
```

As a *positive side-effect*, this organizes your modules in the package explorer in the same way you read hexagonal architecture diagrams: From **the left to the right side**.

## app
This module is the **application layer**. This is already inside the hexagon. Here are the **input ports**. On this point an important question is raised:

Should an **input port** be a java *interface* or a *concrete class* since the **input port** are implemented by *application services* inside the hexagon?

1. The use of **interfaces** just makes sense **if it was packaged alone** in its maven module (in other words, in a separated jar) **to stop the transitive dependency** from the **api** to the **domain**. If you do that, you wouldn't need the `maven exclusions` anymore since the `api module` would reference just the separated interfaces module.
2. I prefer to use concrete classes as **input ports** and let `maven exclusions` do the job of stoping the transitive dependency. I explain my preference in the below topic called ***The 4 ways of stoping transitive dependencies***.

## domain
This module is the classical layer with aggregates, entities, value objects, and domain services. *Some* domain services are technical and must be declared as **interfaces**. Those are **output ports**, for example, repositories interfaces.

## infra
This module has two responsibilities:

1. The classical responsibility of implementing technical **output adapters** like repositories.
2. Launch the application with `SpringApplication.run` through the *java main class* and set up the dependency injection with `@Bean` and others. I could move the `Launcher & Config` to another maven module, but I prefer to stay cleaner.

## The 4 ways of stoping transitive dependencies

Indeed, there are **4 ways** of stoping transitive dependencies. 

1. Using **maven exclusions** (mentioned before). That's my choice. Note that this approach is made on the *dependent side*. There is no problem with it because the `app artifact` never will be re-used outside this project since this is a microservice and exposes its behavior through a rest api.
2. Using a **separated module for *input ports*** (also mentioned before). That's the purest way of doing that. The cost is to add some overhead to the project structure and code.
3. Using **Java 9 Modules** (Jigsaw). In this approach, the job is done on the `dependency side` which is more appropriate when occurs *reuse*. I skipped this because the Java 9 modules require more confs and frequent updates on `module-info.java`. This also forces you to do some configuration in the IDE (for example, disable warning in eclipse) and to add more things in the pom to make it work with annotation processors tools like Lombok. 
4. Using **architecture checks** in build time.

## Architecture checks in build time

In this sample project, I have used the amazing [archuinit](https://archunit.org) tool to double-check the architecture constraints. This is configured and ready to add more sophisticated checks in the future. Please, check the test class `com.thesystem.test.ArchitectureTest`.