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

This maven module belongs to the **infra layer** (outside the hexagon) because it hosts the **input adapters** in the form of `@RestController`. This **must be placed** in a separated module (outside the *infra module*) due to one strong reason:

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

You might know this is not the unique technique to stop transitive dependencies, but you **ALWAYS** will need to make the *api* a separated maven module, no matter the technique. We will discuss more this in the section ***The 4 ways of stoping transitive dependencies***.

## app
This module is the **application layer**. This is already inside the hexagon. Here are the **input ports**. On this point an important question is raised:

Should an **input port** port be a java *interface* or a *concrete class*? Wouldn't be a waste declaring an interface since the input port are implemented just *one time* by *application services* inside the hexagon?

1. The use of **interfaces** makes sense **if they are packaged alone** in its maven module (in other words, in a separated jar) **to stop the transitive dependency** from the **api** to the **domain**. If you do that, you wouldn't need the `maven exclusions` anymore since the `api module` would reference just the separated interfaces module.
2. I prefer to use concrete classes as **input ports** and let `maven exclusions` do the job of stoping the transitive dependencies. I can see the comparison of techniques in the section ***The 4 ways of stoping transitive dependencies***.

## domain
This module is the classical layer with aggregates, entities, value objects, and domain services. *Some* domain services are technical and must be declared as **interfaces**. Those are **output ports**, for example, repositories interfaces.

## infra
This module has two responsibilities:

1. The classical responsibility of implementing technical **output adapters** like repositories.
2. Launch the application with `SpringApplication.run` through the *java main class* and set up the dependency injection with `@Bean` and others. I could move the `Launcher & Config` to another maven module, but I prefer to stay cleaner.

## The 4 ways of stoping transitive dependencies

1. Using **maven exclusions** (mentioned before). It easily does the job without side effects. Note it's configured on the `dependent side`.
2. Using a **separated module for *input ports*** (also mentioned before). That's the purest way of doing that (software design). The cost is to add some overhead to the project structure and code.
3. Using **Java 9 Modules** (Jigsaw). That requires management of the `module-info.java` hierarchy that produces some noise. That's also, in some sense, a duplication of maven's work. Finally, you will be forced to do extra configurations to make maven works with annotation processors tools like Lombok. Note it's configured on the `dependency side`.
4. Using **architecture checks** in build time.

## Architecture checks in build time

In this sample project, I have used the amazing [archuinit](https://archunit.org) tool to double-check the architecture constraints. This is configured and ready to add more sophisticated checks in the future. Please, check the test class `com.thesystem.test.ArchitectureTest`.
