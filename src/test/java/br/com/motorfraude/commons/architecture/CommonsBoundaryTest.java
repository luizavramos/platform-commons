package br.com.motorfraude.commons.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T020 — o recorte do plano 000 §6: {@code platform-commons} não depende de Spring nem de Kafka Streams. Assim o
 * receiver não herda Kafka Streams e o motor não herda Spring. (O app Spring do aceite da H3 vive em src/test e é
 * ignorado aqui.)
 */
class CommonsBoundaryTest {

    @Test
    @DisplayName("o código principal do commons não importa Spring nem Kafka Streams")
    void commonsNaoDependeDeSpringNemKafkaStreams() {
        var mainClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("br.com.motorfraude.commons");

        noClasses()
                .that()
                .resideInAPackage("br.com.motorfraude.commons..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "org.apache.kafka.streams..")
                .because("platform-commons serve a todos os serviços; Spring e Kafka Streams ficam fora dele (plan 000 §6)")
                .check(mainClasses);
    }
}
