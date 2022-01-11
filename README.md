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

You might know this is not the unique technique to stop transitive dependencies, but you **ALWAYS** will need to make the *api* a separated maven module, no matter the technique. We will discuss more this in the section ***The 3 ways of stoping compile-time transitive dependencies***.

## app
This module is the **application layer**. This is already inside the hexagon. Here are the **input ports**. On this point an important question is raised:

Should an **input port** port be a java *interface* or a *concrete class*? Wouldn't be a waste declaring an interface since the input port are implemented just *one time* by *application services* inside the hexagon?

1. The use of **interfaces** makes sense **if they are packaged alone** in its maven module (in other words, in a separated jar) **to stop the transitive dependency** from the **api** to the **domain**. If you do that, you wouldn't need the `maven exclusions` anymore since the `api module` would reference just the separated interfaces module.
2. I prefer to use concrete classes as **input ports** and let `maven exclusions` do the job of stoping the transitive dependencies. You can see the comparison of techniques in the section ***The 3 ways of stoping compile-time transitive dependencies***.
3. DTOs:
    * I only use DTOs in the **application layer**. I mean that the **api** accepts and returns the **very same DTOs** from the application layer. I think declaring exclusive DTOs for the api module is a waste that forces extra mapping.
    * `maven exclusions` will protect the `api module` from **returning** and **receiving** *domain objects* as parameters (*compile-time dependency*) but will not prevent DTOs having *domain objects* **inside** them (*runtime dependency*). To prevent this I do some **runtime checks** in *build time* using the amazing [archuinit](https://archunit.org) tool. Please, see the test class `com.thesystem.test.ArchitectureTest`.
    * Sometimes you want to deliver a **DTO that crosses many aggregate boundaries in a very fast fashion** (e.g. sql joins). Most of the time your `application services` will be **concrete classes**, but for this case, you can declare it as an **interface** and implement it in the **infra layer**. This way, you *will bypass* the **domain layer** and have no problem doing this for **queries**.

## domain
This module is the classical layer with aggregates, entities, value objects, and domain services. *Some* domain services are technical and must be declared as **interfaces**. Those are **output ports**, for example, repositories interfaces.

## infra
This module has two responsibilities:

1. The classical responsibility of implementing technical **output adapters** like repositories.
2. Launch the application with `SpringApplication.run` through the *java main class* and set up the dependency injection with `@Bean` and others. I could move the `Launcher & Config` to another maven module, but I prefer to stay cleaner.

## The 3 ways of stoping compile-time transitive dependencies

1. Using **maven exclusions** (mentioned before). That's easy, but if you want to stop more transitive dependencies (e.g. `spring-tx`), you will need to add **more** maven exclusions. 
2. Using **Java Modules**. That's very good. The `requires` directive, when used alone, stops **all** transitive dependencies by default. On the other hand, Java Modules require management of the `module-info.java` files hierarchy that produces some noise. That's also, in some sense, a duplication of maven's work. Finally, you will be annoyed in some situations where everything works on the IDE but not in the `mvn` command.
3. Using a **separated module for *input ports*** (also mentioned before). That's the purest way of doing that. Also, there is **no need** for `runtime checks` in build time (*the first two options need*). The cost is you can't pass domain objects to the DTOs constructor. You are going to need a `domain to dto` mapper class living in the `application services`. I think this extra module looks like overhead in the project structure (maybe you think differently).
