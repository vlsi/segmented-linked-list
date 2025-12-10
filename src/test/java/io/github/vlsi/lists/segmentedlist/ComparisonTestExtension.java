package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.stream.Stream;


public class ComparisonTestExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(Implementation.values()).map(ComparisonTestTemplateInvocationContext::new);
    }

    private static class ComparisonTestTemplateInvocationContext implements TestTemplateInvocationContext {
        final TestedListProvider provider;

        ComparisonTestTemplateInvocationContext(Implementation implementation) {
            provider = new TestedListProvider(implementation);
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return provider.implementationName();
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return List.of(new ParameterResolver() {

                @Override
                public boolean supportsParameter(ParameterContext parameterContext,
                                                 ExtensionContext extensionContext)
                        throws ParameterResolutionException {
                    return parameterContext.getParameter().getType() == TestedListProvider.class;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext,
                                               ExtensionContext extensionContext)
                        throws ParameterResolutionException {
                    return provider;
                }
            });
        }
    }
}

