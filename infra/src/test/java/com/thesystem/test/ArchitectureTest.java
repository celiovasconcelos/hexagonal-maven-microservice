package com.thesystem.test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClass.Predicates;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

class ArchitectureTest {

  private static final String SYSTEM_PACKAGE = "com.thesystem";
  private static final String DTO_PACKAGES = SYSTEM_PACKAGE + ".app.dtos..";
  private static final String DOMAIN_PACKAGES = SYSTEM_PACKAGE + ".domain..";

  private JavaClasses allClasses = new ClassFileImporter().importPackages(SYSTEM_PACKAGE);

  @Test
  void dtosShouldNotHaveMethodsThatReturnDomainTypes() {
    ArchRule archRule = methods().that()
        .areDeclaredInClassesThat()
        .resideInAPackage(DTO_PACKAGES)
        .should()
        .notHaveRawReturnType(classesThatBelongToTheDomainLayer());
    archRule.check(allClasses);
  }

  @Test
  void dtosShouldNotHaveFieldsOfDomainTypes() {
    ArchRule archRule = fields().that()
        .areDeclaredInClassesThat()
        .resideInAPackage(DTO_PACKAGES)
        .should()
        .notHaveRawType(classesThatBelongToTheDomainLayer());
    archRule.check(allClasses);
  }

  private DescribedPredicate<JavaClass> classesThatBelongToTheDomainLayer() {
    return Predicates.resideInAPackage(DOMAIN_PACKAGES);
  }

}
