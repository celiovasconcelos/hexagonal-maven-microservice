package com.thesystem.test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

class ArchitectureTest {

  private static final String SYSTEM_PACKAGE = "com.thesystem";
  private static final String DOMAIN_PACKAGES = SYSTEM_PACKAGE + ".domain..";
  private static final String APPLICATION_PACKAGES = SYSTEM_PACKAGE + ".app..";
  private static final String INFRA_PACKAGES = SYSTEM_PACKAGE + ".infra..";

  @Test
  void checkIfDomainObjectsAreLeakingToTheAPILayer() {
    JavaClasses classes = new ClassFileImporter().importPackages("com.thesystem");
    ArchRule archRule = classes().that().resideInAPackage(DOMAIN_PACKAGES).should().onlyHaveDependentClassesThat()
        .resideInAnyPackage(DOMAIN_PACKAGES, APPLICATION_PACKAGES, INFRA_PACKAGES);
    archRule.check(classes);
  }

}
