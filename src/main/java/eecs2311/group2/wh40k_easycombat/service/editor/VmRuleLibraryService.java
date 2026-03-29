package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.RuleEditorListItem;
import eecs2311.group2.wh40k_easycombat.util.AppPaths;
import eecs2311.group2.wh40k_easycombat.util.BundledResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class VmRuleLibraryService {
    public List<RuleEditorListItem> getBuiltInRules() {
        try {
            BundledResourceManager.ensureBundledResourcesAvailable();
        } catch (IOException e) {
            return List.of();
        }

        Path dslFolder = AppPaths.getBundledDslDirectory();
        if (!Files.isDirectory(dslFolder)) {
            return List.of();
        }

        List<RuleEditorListItem> items = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(dslFolder)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".rule"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .forEach(path -> items.add(readBuiltInRule(dslFolder, path)));
        } catch (IOException e) {
            return List.of();
        }

        return items;
    }

    private RuleEditorListItem readBuiltInRule(Path dslFolder, Path path) {
        try {
            String script = Files.readString(path);
            String relative = dslFolder.relativize(path).toString().replace('\\', '/');
            return RuleEditorListItem.fromBuiltIn(path.getFileName().toString(), script, "resources/dsl/" + relative);
        } catch (IOException e) {
            return RuleEditorListItem.fromBuiltIn(path.getFileName().toString(), "# Failed to read built-in VM rule.", "resources/dsl");
        }
    }
}
