package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.RuleEditorListItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class VmRuleLibraryService {
    private static final Path DSL_FOLDER = Path.of("src", "main", "resources", "dsl");

    public List<RuleEditorListItem> getBuiltInRules() {
        if (!Files.isDirectory(DSL_FOLDER)) {
            return List.of();
        }

        List<RuleEditorListItem> items = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(DSL_FOLDER)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".rule"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .forEach(path -> items.add(readBuiltInRule(path)));
        } catch (IOException e) {
            return List.of();
        }

        return items;
    }

    private RuleEditorListItem readBuiltInRule(Path path) {
        try {
            String script = Files.readString(path);
            String relative = DSL_FOLDER.relativize(path).toString().replace('\\', '/');
            return RuleEditorListItem.fromBuiltIn(path.getFileName().toString(), script, "resources/dsl/" + relative);
        } catch (IOException e) {
            return RuleEditorListItem.fromBuiltIn(path.getFileName().toString(), "# Failed to read built-in VM rule.", "resources/dsl");
        }
    }
}
