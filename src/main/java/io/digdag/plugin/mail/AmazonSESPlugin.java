package io.digdag.plugin.mail;

import io.digdag.spi.OperatorFactory;
import io.digdag.spi.OperatorProvider;
import io.digdag.spi.Plugin;
import io.digdag.spi.TemplateEngine;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class AmazonSESPlugin implements Plugin
{
    @Override
    public <T> Class<? extends T> getServiceProvider(Class<T> type)
    {
        if (type == OperatorProvider.class) {
            return AmazonSESOperatorProvider.class.asSubclass(type);
        }
        else {
            return null;
        }
    }

    private static class AmazonSESOperatorProvider
            implements OperatorProvider
    {
        @Inject
        protected TemplateEngine templateEngine;

        @Override
        public List<OperatorFactory> get()
        {
            return Collections.singletonList(
                    new AmazonSESOperatorFactory(templateEngine));
        }
    }
}
