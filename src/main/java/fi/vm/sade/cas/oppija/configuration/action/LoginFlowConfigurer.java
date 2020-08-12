package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class LoginFlowConfigurer extends AbstractCasWebflowConfigurer {

    public LoginFlowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                      final ApplicationContext applicationContext,
                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow loginFlow = super.getLoginFlow();
        String f = loginFlow.toString();
        f = f + "1";// Magic happens; Call 'super' to see what you have access to...
    }
}