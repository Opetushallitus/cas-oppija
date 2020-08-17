package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.context.ApplicationContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.*;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class LoginFlowConfigurer extends AbstractCasWebflowConfigurer {


    private static final String TRANSITION_ID_LOGOUT = "logout";

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
            Transition t = super.createTransitionForState(state, "PreventedError", "hopophop");
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

        // fix delegatedAuthenticationAction success transition
        ActionState realSubmitState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        TransitionDefinition successTransition = realSubmitState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        String successTargetStateId = successTransition.getTargetStateId();
        TransitionableState state = getState(getLoginFlow(), CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, successTargetStateId, true);

        // add delegatedAuthenticationAction cancel transition
        EndState cancelState = super.createEndState(getLoginFlow(), CasWebflowConstants.TRANSITION_ID_CANCEL,
                '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());

        // add delegatedAuthenticationAction logout transition
        createTransitionForState(state, TRANSITION_ID_LOGOUT, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);

        // add saml service provider initiated logout support
        setLogoutFlowDefinitionRegistry(super.logoutFlowDefinitionRegistry);
        TransitionableState startState = getStartState(getLogoutFlow());
        ActionState singleLogoutPrepareAction = createActionState(getLogoutFlow(), "samlLogoutPrepareAction",
                new SamlLogoutPrepareAction(ticketGrantingTicketCookieGenerator, ticketRegistrySupport));
        createStateDefaultTransition(singleLogoutPrepareAction, startState.getId());
        setStartState(getLogoutFlow(), singleLogoutPrepareAction);
        DecisionState finishLogoutState = getState(getLogoutFlow(), CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
        ActionList entryActionList = finishLogoutState.getEntryActionList();
        clear(entryActionList, entryActionList::remove);
        entryActionList.add(new SamlLogoutExecuteAction(clients));


    }
}