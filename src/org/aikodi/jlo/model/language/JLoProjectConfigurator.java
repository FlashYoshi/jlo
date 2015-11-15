package org.aikodi.jlo.model.language;

import be.kuleuven.cs.distrinet.jnome.input.LazyJavaFileInputSourceFactory;
import be.kuleuven.cs.distrinet.jnome.workspace.JavaProjectConfiguration;
import be.kuleuven.cs.distrinet.rejuse.predicate.SafePredicate;
import be.ugent.chameleonsupport.language.BaseLibraryJavaProjectConfigurator;
import org.aikodi.chameleon.core.language.Language;
import org.aikodi.chameleon.workspace.*;

import java.io.File;
import java.net.URL;

public class JLoProjectConfigurator extends BaseLibraryJavaProjectConfigurator {

    private static final String JLO_BASE_LIBRARY_DIRECTORY = "base_library/src";

    /**
     * Set baseLibraryInParent to true when running from class files outside eclipse.
     * Set baseLibraryInParent to false when running from class files inside eclipse.
     * When run from a jar, the base library is located correctly.
     */
    public JLoProjectConfigurator() {
        super();
    }

    public static File jloBase() {
        URL url = JLoProjectConfiguration.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            File file = new File(url.toURI());
            String path = file.getAbsolutePath();
            File result;
            if (path.endsWith(".jar")) {
                result = new File(path);
            } else {
                result = new File(file, JLO_BASE_LIBRARY_DIRECTORY);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected JavaProjectConfiguration createProjectConfig(View view) throws ConfigException {
        return new JLoProjectConfiguration(view, new LazyJavaFileInputSourceFactory());
    }

    @Override
    protected void addBaseLibraries(View view, BaseLibraryConfiguration baseLibraryConfiguration) {
        super.addBaseLibraries(view, baseLibraryConfiguration);

        LanguageBaseLibraryConfigurator jLoBaseLibraryConfigurator = new LanguageBaseLibraryConfigurator(language(), "jlo");
        jLoBaseLibraryConfigurator.process(view, baseLibraryConfiguration);
    }

    @Override
    public SafePredicate<? super String> sourceFileFilter() {
        return new ExtensionPredicate("jlo");
    }

    protected Language jlo(Workspace workspace) {
        return language(JLo.NAME, workspace);
    }

    public static class BaseDirectoryLoader extends DirectoryScanner {

        public BaseDirectoryLoader(String root, FileDocumentLoaderFactory factory, SafePredicate<? super String> filter) {
            super(root, filter, true, factory);
        }

    }


}
