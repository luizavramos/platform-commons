package br.com.motorfraude.commons.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T020 — as regras reutilizáveis aceitam código conforme e PEGAM violações plantadas. Sem este teste, uma regra com
 * defeito passaria em silêncio em todos os serviços.
 */
class CleanArchitectureRulesTest {

    private static final String FIXTURES = "br.com.motorfraude.commons.architecture.fixtures";
    private static final String GOOD = FIXTURES + ".good";
    private static final String BAD_LAYERS = FIXTURES + ".bad.layers";
    private static final String BAD_FRAMEWORK = FIXTURES + ".bad.framework";
    private static final String BAD_CLOCK = FIXTURES + ".bad.clock";

    private static JavaClasses classesOf(String basePackage) {
        return new ClassFileImporter().importPackages(basePackage);
    }

    @Test
    @DisplayName("código conforme passa em todas as regras")
    void codigoConformePassa() {
        assertThatCode(() -> CleanArchitectureRules.assertAll(classesOf(GOOD), GOOD)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("domínio dependendo de adapter e aplicação dependendo de infraestrutura violam a regra de camadas")
    void violacaoDeCamadasEPega() {
        var classes = classesOf(BAD_LAYERS);

        var result = CleanArchitectureRules.layersRespectDependencyRule(BAD_LAYERS).evaluate(classes);

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString()).contains("Order").contains("PlaceOrder");
        assertThatThrownBy(() -> CleanArchitectureRules.assertAll(classes, BAD_LAYERS)).isInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("Kafka no domínio e Spring na aplicação violam a regra de miolo sem framework")
    void violacaoDeFrameworkEPega() {
        var result = CleanArchitectureRules.innerLayersAreFrameworkFree(BAD_FRAMEWORK).evaluate(classesOf(BAD_FRAMEWORK));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString()).contains("Event").contains("Checks");
    }

    @Test
    @DisplayName("Instant.now(), Clock.systemUTC() e System.currentTimeMillis() fora da infraestrutura violam a regra do relógio")
    void violacaoDeRelogioEPega() {
        var result = CleanArchitectureRules.noDirectClockAccess(BAD_CLOCK).evaluate(classesOf(BAD_CLOCK));

        assertThat(result.hasViolation()).isTrue();
        var report = result.getFailureReport().toString();
        assertThat(report).contains("Deadline").contains("Stamp");
        assertThat(report).contains("Instant.now()").contains("Clock.systemUTC()").contains("System.currentTimeMillis()");
    }

    @Test
    @DisplayName("cada regra pega só o que é dela: as violações plantadas não vazam para as outras regras")
    void regrasSaoIndependentes() {
        assertThat(CleanArchitectureRules.layersRespectDependencyRule(BAD_FRAMEWORK).evaluate(classesOf(BAD_FRAMEWORK)).hasViolation()).isFalse();
        assertThat(CleanArchitectureRules.layersRespectDependencyRule(BAD_CLOCK).evaluate(classesOf(BAD_CLOCK)).hasViolation()).isFalse();
        assertThat(CleanArchitectureRules.innerLayersAreFrameworkFree(BAD_LAYERS).evaluate(classesOf(BAD_LAYERS)).hasViolation()).isFalse();
        assertThat(CleanArchitectureRules.noDirectClockAccess(BAD_LAYERS).evaluate(classesOf(BAD_LAYERS)).hasViolation()).isFalse();
    }

    @Test
    @DisplayName("a infraestrutura pode criar o relógio de produção (é onde ele nasce)")
    void infraestruturaPodeCriarRelogio() {
        assertThat(CleanArchitectureRules.noDirectClockAccess(GOOD).evaluate(classesOf(GOOD)).hasViolation()).isFalse();
    }

    @Test
    @DisplayName("pacote sem nenhuma classe é erro: evita regra que passa por não ter analisado nada")
    void pacoteVazioFalha() {
        assertThatThrownBy(() -> CleanArchitectureRules.assertAll(classesOf("br.com.motorfraude.nao.existe"), "br.com.motorfraude.nao.existe"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("br.com.motorfraude.nao.existe");
    }
}
