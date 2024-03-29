package fi.vm.sade.cas.oppija.configuration.webflow;

import fi.vm.sade.cas.oppija.CasOppijaConstants;
import fi.vm.sade.cas.oppija.configuration.action.Pac4jClientProvider;
import fi.vm.sade.cas.oppija.configuration.action.SamlLoginPrepareAction;
import fi.vm.sade.cas.oppija.configuration.action.ServiceRedirectAction;
import fi.vm.sade.cas.oppija.configuration.action.StoreServiceParamAction;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.pac4j.core.client.Clients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_SUCCESS;


/**
 * This class should include only fixes to default cas delegated authentication configuration.
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationWebflowConfiguration implements CasWebflowExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedAuthenticationWebflowConfiguration.class);
    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final FlowDefinitionRegistry logoutFlowDefinitionRegistry;
    private final FlowDefinitionRegistry delegationRedirectFlowRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final Clients clients;

    public DelegatedAuthenticationWebflowConfiguration(FlowBuilderServices flowBuilderServices,
                                                       @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                       @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                       @Qualifier("delegatedClientRedirectFlowRegistry") FlowDefinitionRegistry delegationRedirectFlowRegistry,
                                                       ConfigurableApplicationContext applicationContext,
                                                       CasConfigurationProperties casProperties,
                                                       Clients clients) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry;
        this.delegationRedirectFlowRegistry = delegationRedirectFlowRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.clients = clients;
    }

    @Bean
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer() {
        return new DelegatedAuthenticationWebflowConfigurer(
                flowBuilderServices, loginFlowDefinitionRegistry,
                logoutFlowDefinitionRegistry, delegationRedirectFlowRegistry, applicationContext, casProperties
        )
        {

            @Override
            public int getOrder() {
                // This CasWebflowExecutionPlanConfigurer must be run before SurrogateConfiguration to able to cancel auth
                // but after InterruptConfiguration to enable surrogate authentication after delegated authentication
                return Ordered.HIGHEST_PRECEDENCE + 1;
            }
        };
    }

    @Bean
    Pac4jClientProvider clientProvider() {
        return new Pac4jClientProvider(clients);
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {

        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                /* Initial login action to collect url parameters: valtuudet & services
                 StartActionList from loginflow gets run before initial state, so SamlLoginPrepareAction is always run in login flow.
                 It maybe could be replaced with a new decicisionState
                 */
                ActionList startActionList = getLoginFlow().getStartActionList();
                startActionList.add(new SamlLoginPrepareAction(getLoginFlow()));

                /* Delegated authentication state success transition is modified to redirect to STATE_ID_INQUIRE_INTERRUPT_ACTION */
                ActionState interruptActionState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT, ActionState.class);
                TransitionableState delegatedAuthenticationState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
                createTransitionForState(delegatedAuthenticationState, TRANSITION_ID_SUCCESS, interruptActionState.getId(), true);

                // add delegatedAuthenticationAction cancel transition to logout
                EndState cancelState = super.createEndState(getLoginFlow(), CasWebflowConstants.TRANSITION_ID_CANCEL,
                        '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
                createTransitionForState(delegatedAuthenticationState, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());

                // add delegatedAuthenticationAction logout from idp (Suomifi) redirect to login flow. Check delegatedAuthenticationAction.
                ActionState idpLogoutActionState = createActionState(getLoginFlow(), CasOppijaConstants.STATE_ID_IDP_LOGOUT, CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT);
                createTransitionForState(delegatedAuthenticationState, CasOppijaConstants.TRANSITION_ID_IDP_LOGOUT, idpLogoutActionState.getId());
                idpLogoutActionState.getActionList().add(new ServiceRedirectAction());
                createStateDefaultTransition(idpLogoutActionState, CasWebflowConstants.STATE_ID_REDIRECT_VIEW);

                //Logout flow starts from terminate session state, Here we add the storage of return url action to a cookie when logout starts
                setLogoutFlowDefinitionRegistry(DelegatedAuthenticationWebflowConfiguration.this.logoutFlowDefinitionRegistry);
                getLogoutFlow().getStartActionList().add(new StoreServiceParamAction(casProperties));
            }
        });
    }
}

