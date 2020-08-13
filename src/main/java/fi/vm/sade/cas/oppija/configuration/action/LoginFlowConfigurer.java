package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.context.ApplicationContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.*;
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
        if(super.containsFlowState(loginFlow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE)) {
            ActionState state = super.getState(loginFlow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE, ActionState.class);
            ActionList entryActionList = state.getEntryActionList();
            Transition t = super.createTransitionForState(state, "SurrogateNotAllowedException", "hopophop");
            //ts.toArray()[9].setExecutionCriteria();
            TransitionSet ts = state.getTransitionSet();
            f = f + "1";
            //state.add({ requestContext ->
             //       def flowScope = requestContext.flowScope
             //       def httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext)

              //      logger.info("Action executing as part of ${state.id}. Stuff happens...")
           // return new Event(this, "success")
        }
        // Magic happens; Call 'super' to see what you have access to...


        //handleAuthenticationFailure
    }
}