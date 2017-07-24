package egroum;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class AUGCollector {
    private final AUGConfiguration config;
    private final Collection<EGroumGraph> augs = new ArrayList<>();

    public AUGCollector(AUGConfiguration config) {
        this.config = config;
    }

    public void collectFrom(String projectName, Path path, String[] dependencies) {
        ArrayList<EGroumGraph> groums = new EGroumBuilder(config).buildBatch(convertPath(path), dependencies);
        for (EGroumGraph groum : groums) {
            groum.setProjectName(projectName);
            augs.add(groum);
        }
    }

    private String convertPath(Path path) {
        return path.toAbsolutePath().toString();
    }

    public Collection<EGroumGraph> getAUGs() {
        return augs;
    }
}
