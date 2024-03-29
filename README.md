# Hexagonal Architecture with Maven for a Spring Boot microservice

## Motivation

I couldn’t find any good project structure expressing the hexagonal architecture constraints for a Spring Boot microservice. So I decided to create this sample project with my insights.

## The ideal project structure

![](https://i.imgur.com/elepnRz.png)

That's beautiful. The ideal hexagonal architecture requires exactly **six** components. Your microservice project structure will look like this:

* thesystem (parent pom.xml)
  * api *(input adapter)*
  * app-interfaces *(input port)*
  * app-services *(application services)*
  * domain *(domain)*
  * clients *(output adapter)*
  * starter *(launch & config)*

That's perfect, right?

> Not to me!

## Drawbacks

The ideal project structure has some drawbacks to me:

1. I think that six maven modules are too much in the package explorer.
2. I think declaring interfaces in `application interfaces` module and after implementing them in the `application services` module is boring.
3. I like to pass `domain objects` to DTO's constructors, but I can't do that. I must create `domain to dto` mapper classes in the `application services` module.

## Trying to shrink it

Let's try to reduce it to **four** maven modules.

* thesystem (parent pom.xml)
  * api *(input adapter)*
  * app *(input port + application services)*
  * domain *(domain)*
  * infra *(output adapter + launch & config)*

I have merged *(input port + application services)* in the `app` module. But wait:

What was the initial purpose of packing `input ports` in a separated module having only interfaces and DTOs? 

Short answer: **To stop transitive dependency from the `api module` to the `domain`.** 

I saw many projects with application services interfaces and their implementations in the **same module**. There is no sense in doing that. In this situation, `application services` should have no interfaces, **only concrete classes**. 

Unfortunately, after merging, the transitive dependency came back. Now my `domain objects` **can leak** to the `api module` (**outside** the hexagon).

Fortunately, there is hope.

## The other two ways of stopping transitive dependencies

Let's see alternative techniques to solve that new problem.

### Maven exclusions

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

* Maven exclusions will protect the `api module` from **returning** and **receiving** *domain objects* as parameters (*compile-time dependency*). That's good!
* On the other hand, this will not prevent **DTOs** from having *domain objects* **inside** them (*runtime dependency*). Fortunately, I can ensure this won't happen by doing some **runtime checks** in *build time* using the incredible [archunit](https://archunit.org) tool. Please, see the test class `com.thesystem.test.ArchitectureTest`. This tool **allows** me to use `domain objects` in the DTO's **constructor** but **disallows** me to have them as **property or method returns**. That's beautiful.
* Keep in mind if you want to stop more transitive dependencies like `spring-tx`, you will need to add **more** maven exclusions. 

### Java Modules
* The `requires` java modules directive, when used alone, stops **all** transitive dependencies **by default**. That's very good!
* On the other hand, java modules need management of the `module-info.java` files hierarchy that produces some noise. That's also, in some sense, a duplication of maven's work. Finally, you will be annoyed in some situations where everything works on the IDE but not in the `mvn` command.
* You will need those runtime checks with the archunit tool as well.

## infra
The module infra is the sum of *(output adapter + launch & config)*. There aren't side effects here. This merge just makes sense to me.
