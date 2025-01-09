package dev.magicmq.docstranslator.module.init;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InitPyRegistry {

    private final List<InitPy> initPys;

    public InitPyRegistry() {
        this.initPys = new ArrayList<>();
    }

    public boolean doesInitPyExistAt(Path path) {
        return getInitPyAt(path) != null;
    }

    public InitPy getInitPyAt(Path path) {
        for (InitPy initPy : initPys) {
            if (initPy.getPath().equals(path))
                return initPy;
        }
        return null;
    }

    public void newInitPy(Path path) {
        initPys.add(new InitPy(path));
    }

    public void saveInitPys(Path outputFolderPath) {
        for (InitPy initPy : initPys)
            initPy.saveToFolder(outputFolderPath);
    }
}
