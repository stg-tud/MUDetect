package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import egroum.AUGCollector;
import egroum.ContainsTypeUsagePredicate;
import egroum.EGroumGraph;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MuDetectCrossProjectRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner()
                .withDetectOnlyStrategy(new ProvidedPatternsStrategy())
                .withMineAndDetectStrategy(new CrossProjectStrategy())
                .run(args);
    }

    private static class CrossProjectStrategy extends IntraProjectStrategy {
        @SuppressWarnings("unchecked")
        @Override
        Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws FileNotFoundException {
            AUGCollector collector = new AUGCollector(new DefaultAUGConfiguration());
            String targetTypeName = "org.apache.commons.lang.text.StrBuilder";
            for (Object entry : new Yaml().loadAll(getExampleData(targetTypeName))) {
                Map<String, Object> data = (Map<String, Object>) entry;
                String projectPath = (String) data.get("path");
                List<String> srcDirs = (List<String>) data.get("source_paths");
                for (String srcDir : srcDirs) {
                    collector.collectFrom(Paths.get(projectPath, srcDir), args.getDependencyClassPath());
                }
            }
            return collector.getAUGs().stream()
                    .filter(new ContainsTypeUsagePredicate(getTargetTypeSimpleName(targetTypeName)))
                    .collect(Collectors.toList());
        }

        private String getExampleData(String targetType) {
            switch (targetType) {
                case "org.apache.commons.lang.text.StrBuilder":
                    return "---\n" +
                            "id: \"AGETO/gyrex-carbonado-persistence\"\n" +
                            "url: \"http://github.com/AGETO/gyrex-carbonado-persistence\"\n" +
                            "path: \"./checkouts/_examples/AGETO/gyrex-carbonado-persistence/latest/checkout\"\n" +
                            "source_paths: ['bundles/net.ageto.gyrex.persistence.carbonado.jdbc.configurator/src', 'bundles/net.ageto.gyrex.persistence.jdbc.pool/src', 'bundles/net.ageto.gyrex.persistence.carbonado/src', 'bundles/net.ageto.gyrex.persistence.liquibase/src']\n" +
                            "---\n" +
                            "id: \"CKLV/cookinglive\"\n" +
                            "url: \"http://github.com/CKLV/cookinglive\"\n" +
                            "path: \"./checkouts/_examples/CKLV/cookinglive/latest/checkout\"\n" +
                            "source_paths: ['SpringProject/validator', 'SpringProject/web-base', 'SpringProject/velocity-tool-source', 'SpringProject/Source', 'SpringProject/velocity-source']\n" +
                            "---\n" +
                            "id: \"CSTARS/PostgresVizSource\"\n" +
                            "url: \"http://github.com/CSTARS/PostgresVizSource\"\n" +
                            "path: \"./checkouts/_examples/CSTARS/PostgresVizSource/latest/checkout\"\n" +
                            "source_paths: ['google-visualization-java/examples/src/java', 'google-visualization-java/src/main/java', 'src']\n" +
                            "---\n" +
                            "id: \"Danielabeltran/Tesis\"\n" +
                            "url: \"http://github.com/Danielabeltran/Tesis\"\n" +
                            "path: \"./checkouts/_examples/Danielabeltran/Tesis/latest/checkout\"\n" +
                            "source_paths: ['WebHolaMundoVelocity- 22062013/src/java', 'Velocity/Primera Prueba Velocity/PruebaVelocity2/src', 'Jar Velocity y Ejemplos/velocity-1.7/examples/app_example1', 'Jar Velocity y Ejemplos/velocity-1.7/examples/xmlapp_example', 'Jar Velocity y Ejemplos/velocity-1.7/examples/context_example', 'Jar Velocity y Ejemplos/velocity-1.7/examples/event_example', 'Jar Velocity y Ejemplos/velocity-1.7/src/java', 'HolaMundoVelocity/src', 'Jar Velocity y Ejemplos/velocity-1.7/examples/logger_example', 'Jar Velocity y Ejemplos/velocity-1.7/examples/app_example2']\n" +
                            "---\n" +
                            "id: \"Dausleen/dotcms\"\n" +
                            "url: \"http://github.com/Dausleen/dotcms\"\n" +
                            "path: \"./checkouts/_examples/Dausleen/dotcms/latest/checkout\"\n" +
                            "source_paths: ['src', 'docs/examples/plugins/com.dotmarketing.custom_field/src', 'docs/examples/osgi/com.dotcms.actionlet/src', 'docs/examples/osgi/com.dotcms.spring/src', 'docs/examples/osgi/com.dotcms.override/src', 'docs/examples/osgi/com.dotcms.servlet/src', 'docs/examples/osgi/com.dotcms.service/src', 'docs/examples/osgi/com.dotcms.hooks/src', 'docs/examples/osgi/com.dotcms.3rd.party/src', 'docs/examples/osgi/com.dotcms.viewtool/src', 'docs/examples/CMIS', 'docs/examples/plugins/hello.world/src']\n" +
                            "---\n" +
                            "id: \"Phoenix1708/t2-server-jar-android-0.1\"\n" +
                            "url: \"http://github.com/Phoenix1708/t2-server-jar-android-0.1\"\n" +
                            "path: \"./checkouts/_examples/Phoenix1708/t2-server-jar-android-0.1/latest/checkout\"\n" +
                            "source_paths: ['t2-server-jar-android-0.1-hyde/src/main/java', 'src/main/java']\n" +
                            "---\n" +
                            "id: \"RoyalDev/RoyalAuth\"\n" +
                            "url: \"http://github.com/RoyalDev/RoyalAuth\"\n" +
                            "path: \"./checkouts/_examples/RoyalDev/RoyalAuth/latest/checkout\"\n" +
                            "source_paths: ['src/main/java']\n" +
                            "---\n" +
                            "id: \"RoyalDev/RoyalChat\"\n" +
                            "url: \"http://github.com/RoyalDev/RoyalChat\"\n" +
                            "path: \"./checkouts/_examples/RoyalDev/RoyalChat/latest/checkout\"\n" +
                            "source_paths: ['src/main/java']\n" +
                            "---\n" +
                            "id: \"RoyalDev/RoyalCommands\"\n" +
                            "url: \"http://github.com/RoyalDev/RoyalCommands\"\n" +
                            "path: \"./checkouts/_examples/RoyalDev/RoyalCommands/latest/checkout\"\n" +
                            "source_paths: ['modules/NMS1_6_R1/src/main/java', 'modules/NMS1_7_R2/src/main/java', 'modules/RoyalCommands/src/main/java', 'modules/NoSupport/src/main/java', 'modules/NMS1_6_R3/src/main/java', 'modules/NMS1_7_R1/src/main/java', 'modules/API/src/main/java', 'modules/NMS1_8_R1/src/main/java', 'modules/NMS1_8_R2/src/main/java', 'modules/NMS1_6_R2/src/main/java', 'modules/NMS1_7_R3/src/main/java', 'modules/NMS1_7_R4/src/main/java']\n" +
                            "---\n" +
                            "id: \"Sandip-Adisare/ProjectAgri\"\n" +
                            "url: \"http://github.com/Sandip-Adisare/ProjectAgri\"\n" +
                            "path: \"./checkouts/_examples/Sandip-Adisare/ProjectAgri/latest/checkout\"\n" +
                            "source_paths: ['admin', 'user', 'web', 'core']";
                default:
                    throw new IllegalArgumentException("no example data for this type: " + targetType);
            }
        }

        private String getTargetTypeSimpleName(String targetTypeName) {
            return targetTypeName.substring(targetTypeName.lastIndexOf('.') + 1);
        }
    }
}
