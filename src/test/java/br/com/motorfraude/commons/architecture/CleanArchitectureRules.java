package br.com.motorfraude.commons.architecture;

import static com.tngtech.archunit.lang.conditions.ArchConditions.callMethod;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public final class CleanArchitectureRules {

    public static final List<String> FORBIDDEN_IN_INNER_LAYERS = List.of(
            "org.springframework..",
            "org.apache.kafka..",
            "java.sql..",
            "javax.sql..",
            "jakarta.persistence..",
            "org.postgresql..",
            "org.flywaydb..",
            "com.zaxxer..",
            "redis.clients..",
            "io.lettuce..",
            "com.fasterxml.jackson..",
            "tools.jackson..");

    private static final String DOMAIN = "domain";
    private static final String APPLICATION = "application";
    private static final String ADAPTERS = "adapters";
    private static final String INFRASTRUCTURE = "infrastructure";

    private CleanArchitectureRules() {}

    public static ArchRule layersRespectDependencyRule(String basePackage) {
        return layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer(DOMAIN).definedBy(basePackage + ".domain..")
                .layer(APPLICATION).definedBy(basePackage + ".application..")
                .layer(ADAPTERS).definedBy(basePackage + ".adapters..")
                .layer(INFRASTRUCTURE).definedBy(basePackage + ".infrastructure..")
                .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(APPLICATION, ADAPTERS, INFRASTRUCTURE)
                .whereLayer(APPLICATION).mayOnlyBeAccessedByLayers(ADAPTERS, INFRASTRUCTURE)
                .whereLayer(ADAPTERS).mayOnlyBeAccessedByLayers(INFRASTRUCTURE)
                .whereLayer(INFRASTRUCTURE).mayNotBeAccessedByAnyLayer()
                .because("a regra de dependência da Clean Architecture: só se depende de camadas mais internas");
    }

    public static ArchRule innerLayersAreFrameworkFree(String basePackage) {
        return noClasses()
                .that()
                .resideInAnyPackage(basePackage + ".domain..", basePackage + ".application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(FORBIDDEN_IN_INNER_LAYERS.toArray(String[]::new))
                .because("domínio e casos de uso não conhecem frameworks nem infraestrutura");
    }

    public static ArchRule noDirectClockAccess(String basePackage) {
        ArchCondition<JavaClass> condition = callMethod(Instant.class, "now");
        for (Class<?> type : List.of(LocalDateTime.class, LocalDate.class, LocalTime.class, ZonedDateTime.class, OffsetDateTime.class)) {
            condition = condition.or(callMethod(type, "now"));
        }
        condition = condition
                .or(callMethod(System.class, "currentTimeMillis"))
                .or(callMethod(Clock.class, "systemUTC"))
                .or(callMethod(Clock.class, "systemDefaultZone"));

        return noClasses()
                .that()
                .resideInAnyPackage(basePackage + ".domain..", basePackage + ".application..", basePackage + ".adapters..")
                .should(condition)
                .because("toda dependência de 'agora' passa pela porta Clock (constituição VIII)");
    }

    public static List<ArchRule> all(String basePackage) {
        return List.of(
                layersRespectDependencyRule(basePackage),
                innerLayersAreFrameworkFree(basePackage),
                noDirectClockAccess(basePackage));
    }

    public static void assertAll(JavaClasses classes, String basePackage) {
        if (classes.isEmpty()) {
            throw new AssertionError("Nenhuma classe encontrada em '" + basePackage + "': o pacote base está certo?");
        }
        all(basePackage).forEach(rule -> rule.check(classes));
    }
}
