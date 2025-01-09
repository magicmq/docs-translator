package dev.magicmq.docstranslator;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import dev.magicmq.docstranslator.module.Module;
import dev.magicmq.docstranslator.module.init.InitPyRegistry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JdkTranslator {

    private final Path javaSourcesPath;
    private final Path outputFolder;
    private final InitPyRegistry registry;
    private final List<Path> neededSourcesPaths;

    public JdkTranslator(Path javaSourcesPath, Path outputFolder, InitPyRegistry registry) {
        this.javaSourcesPath = javaSourcesPath;
        this.outputFolder = outputFolder;
        this.registry = registry;
        neededSourcesPaths = new ArrayList<>();
    }

    public void addSourceFile(String packageName, String className) {
        Path sourceFilePath = Path.of(packageName.replace(".", "/")).resolve(Path.of(className + ".java"));
        if (!neededSourcesPaths.contains(sourceFilePath))
            neededSourcesPaths.add(sourceFilePath);
    }

    public void translateSources() {
        for (Path sourceFilePath : neededSourcesPaths) {
            for (Path parentPath : getParentPaths(sourceFilePath)) {
                generateInitPy(parentPath);
            }

            try {
                String sourceFileName = sourceFilePath.getFileName().toString();
                String className = sourceFileName.substring(0, sourceFileName.lastIndexOf("."));

                Path absoluteSourcePath = javaSourcesPath.resolve(sourceFilePath);

                DocsTranslator.get().getLogger().log(Level.FINE, "Processing JDK source file '" + sourceFileName + "'");

                String translated = translateSource(absoluteSourcePath, className);
                Path outputFilePath = outputFolder.resolve(sourceFilePath.getParent()).resolve(className + ".py");
                saveTranslatedFile(outputFilePath, translated);

                DocsTranslator.get().getLogger().log(Level.FINE, "Finished processing JDK source file '" + sourceFileName + "'");
            } catch (IOException e) {
                DocsTranslator.get().getLogger().log(Level.SEVERE, "Error when processing JDK source file '" + sourceFilePath.getFileName().toString() + "'");
                e.printStackTrace();
            }
        }
    }

    private String translateSource(Path sourceFilePath, String className) throws IOException {
        String fileContent = Files.readString(sourceFilePath);

        CompilationUnit cu = new JavaParser().parse(fileContent).getResult().orElseThrow();

        String packageName = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");

        Module module = new Module(
                DocsTranslator.get().getSettings().getJdkSources().getName(),
                DocsTranslator.get().getSettings().getJdkSources().getName(),
                packageName,
                className,
                this
        );
        module.init(cu);

        return module.translate();
    }

    private void saveTranslatedFile(Path outputFilePath, String text) throws IOException {
        Files.createDirectories(outputFilePath.getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.toFile()))) {
            writer.write(text);
        } catch (IOException e) {
            DocsTranslator.get().getLogger().log(Level.SEVERE, "Error when saving file '" + outputFilePath.getFileName().toString() + "' to output folder", e);
            e.printStackTrace();
        }
    }

    private List<Path> getParentPaths(Path path) {
        List<Path> parentPaths = new ArrayList<>();

        Path parent = path.getParent();
        while (parent != null) {
            parentPaths.add(parent);
            parent = parent.getParent();
        }

        return parentPaths;
    }

    private void generateInitPy(Path folder) {
        if (!registry.doesInitPyExistAt(folder)) {
            registry.newInitPy(folder);
        }
    }

}