package org.aikodi.jlo.build;

import be.kuleuven.cs.distrinet.jnome.core.language.Java7;
import be.kuleuven.cs.distrinet.jnome.core.language.Java7LanguageFactory;
import be.kuleuven.cs.distrinet.jnome.workspace.JavaView;
import be.ugent.chameleonsupport.build.LanguageBuilder;
import org.aikodi.jlo.model.language.JLo;
import org.aikodi.jlo.translate.JLoToJava8Translator;
import org.aikodi.chameleon.core.namespace.LazyRootNamespace;
import org.aikodi.chameleon.exception.ChameleonProgrammerException;
import org.aikodi.chameleon.plugin.ViewPlugin;
import org.aikodi.chameleon.plugin.output.Syntax;
import org.aikodi.chameleon.workspace.View;

public class JLoBuilder extends LanguageBuilder<JLo, Java7> {

    public JLoBuilder(View view) {
        super(view);
    }

    @Override
    public <T extends ViewPlugin> void setContainer(View view, Class<T> pluginInterface) {
        if (!(view.language() instanceof JLo)) {
            throw new ChameleonProgrammerException();
        }

        super.setContainer(view, pluginInterface);
        Java7 target = new Java7LanguageFactory().create();
        JavaView targetView = new JavaView(new LazyRootNamespace(), target);
        translator = new JLoToJava8Translator(view, targetView);

        if (syntax != null) {
            target.setPlugin(Syntax.class, syntax);
        }
    }
}
