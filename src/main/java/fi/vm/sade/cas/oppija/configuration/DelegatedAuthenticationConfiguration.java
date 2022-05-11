package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.cas.oppija.configuration.action.*;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.http.HttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.*;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_SUCCESS;


/**
 * This class should include only fixes to default cas delegated authentication configuration.
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationConfiguration implements CasWebflowExecutionPlanConfigurer, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedAuthenticationConfiguration.class);
    private static final String TRANSITION_ID_LOGOUT = "logout";
    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final FlowDefinitionRegistry logoutFlowDefinitionRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext;
    private final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final Clients clients;

    public DelegatedAuthenticationConfiguration(FlowBuilderServices flowBuilderServices,
                                                @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                ConfigurableApplicationContext applicationContext,
                                                CasConfigurationProperties casProperties,
                                                DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
                                                DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager,
                                                @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER) CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                TicketRegistrySupport ticketRegistrySupport,
                                                Clients clients) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.delegatedClientAuthenticationConfigurationContext = delegatedClientAuthenticationConfigurationContext;
        this.delegatedClientAuthenticationWebflowManager = delegatedClientAuthenticationWebflowManager;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.clients = clients;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        // this is from default delegatedAuthenticationWebflowConfigurer bean:
        plan.registerWebflowConfigurer(new DelegatedAuthenticationWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, logoutFlowDefinitionRegistry, applicationContext,
                casProperties));

        LOGGER.debug("default web flow configured");
        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // Initial login action to collect url parameters: valtuudet & services
                ActionList startActionList = getLoginFlow().getStartActionList();
                startActionList.add(new SamlLoginPrepareAction(getLoginFlow()));
                LOGGER.trace("configuring additional web flow, url parameters collected");

                // fix delegatedAuthenticationAction success transition
                ActionState realSubmitState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
                TransitionDefinition successTransition = realSubmitState.getTransition(TRANSITION_ID_SUCCESS);
                String successTargetStateId = successTransition.getTargetStateId();
                LOGGER.trace("ActionState {} has transition {} with target state: {} ", CasWebflowConstants.STATE_ID_REAL_SUBMIT, TRANSITION_ID_SUCCESS, successTargetStateId );
                TransitionableState state = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
                createTransitionForState(state, TRANSITION_ID_SUCCESS, successTargetStateId, true);
                LOGGER.trace("configuring additional web flow, State {}, transition {} target is set also to {}",CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION, TRANSITION_ID_SUCCESS, successTargetStateId);
                // add delegatedAuthenticationAction cancel transition
                EndState cancelState = super.createEndState(getLoginFlow(), CasWebflowConstants.TRANSITION_ID_CANCEL,
                        '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
                createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());
                LOGGER.trace("configuring additional web flow, delegatedAuthenticationAction cancel transition target is now:{}", cancelState.getId());
                // add delegatedAuthenticationAction logout transition
                createTransitionForState(state, TRANSITION_ID_LOGOUT, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
                LOGGER.trace("configuring additional web flow, delegatedAuthenticationAction logout transition");
                // add saml service provider initiated logout support
                setLogoutFlowDefinitionRegistry(DelegatedAuthenticationConfiguration.this.logoutFlowDefinitionRegistry);
                TransitionableState startState = getStartState(getLogoutFlow());
                ActionState singleLogoutPrepareAction = createActionState(getLogoutFlow(), "samlLogoutPrepareAction",
                        new SamlLogoutPrepareAction(ticketGrantingTicketCookieGenerator, ticketRegistrySupport));
                createStateDefaultTransition(singleLogoutPrepareAction, startState.getId());
                setStartState(getLogoutFlow(), singleLogoutPrepareAction);
                LOGGER.trace("configuring additional web flow, delegatedAuthenticationAction saml-initiated logout support");

                DecisionState finishLogoutState = getState(getLogoutFlow(), CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
                ActionList entryActionList = finishLogoutState.getEntryActionList();
                clear(entryActionList, entryActionList::remove);
                Pac4jClientProvider clientProvider = new Pac4jClientProvider(clients);
                entryActionList.add(new StoreServiceParamAction(casProperties));
                entryActionList.add(new SamlLogoutExecuteAction(clientProvider));
                entryActionList.add(new ServiceRedirectAction(clientProvider));
                LOGGER.debug("default web flow customization for delegateAuthentication 1st phase completed");
            }
        });

        plan.registerWebflowConfigurer(new DefaultLogoutWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // add logout flow to login flow to be able logout from delegatedAuthenticationAction
                Flow flow = getLogoutFlow();
                StateDefinition startState = flow.getStartState();
                super.doInitialize();
                flow.setStartState(startState.getId());
                LOGGER.debug("login Flow customized for delegateAuthentication logout");
            }

            @Override
            public Flow getLogoutFlow() {
                return getLoginFlow();
            }
        });
    }

    @Override
    public int getOrder() {
        // This CasWebflowExecutionPlanConfigurer must be run before SurrogateConfiguration to able to cancel auth
        // but after InterruptConfiguration to enable surrogate authentication after delegated authentication
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    // override default delegatedAuthenticationWebflowConfigurer to be able to override its flow definitions (see above)
    @Bean
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer() {
        return new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // nop
            }
        };
    }

    // override default delegatedAuthenticationAction to automatically logout on error
    @Bean
    public Action delegatedAuthenticationAction() {
        return new DelegatedClientAuthenticationAction(
                delegatedClientAuthenticationConfigurationContext,
                delegatedClientAuthenticationWebflowManager
        ) {
            @Override
            public Event doExecute(RequestContext context) {
                try {
                    return super.doExecute(context);
                } catch (Exception e) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
            }

            @Override
            protected Event stopWebflow(Exception e, RequestContext requestContext) {
                if (e instanceof HttpAction) {
                    return handleLogout((HttpAction) e, RequestContextHolder.getRequestContext());
                }
                return super.stopWebflow(e, requestContext);
            }

            private Event handleLogout(HttpAction httpAction, RequestContext requestContext) {
                if (httpAction.getCode() == HttpConstants.TEMPORARY_REDIRECT) {
                    String redirectUrl = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext).getHeader(HttpConstants.LOCATION_HEADER);
                    WebUtils.putLogoutRedirectUrl(requestContext, redirectUrl);
                    return result(TRANSITION_ID_LOGOUT);
                }
                throw new IllegalArgumentException("Unhandled logout response code: " + httpAction.getCode());
            }
        };
    }

    private static <E, T extends Iterable<E>> void clear(T iterable, Consumer<E> remover) {
        StreamSupport.stream(iterable.spliterator(), false).collect(toList()).forEach(remover);
    }

}

