package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.cas.oppija.configuration.action.LoginFlowConfigurer;
import fi.vm.sade.cas.oppija.configuration.action.LogoutFlowConfigurer;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.DelegatedClientNavigationController;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebflowExecutionPlanConfiguration implements CasWebflowExecutionPlanConfigurer {


    private static final String TRANSITION_ID_LOGOUT = "logout";

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("builtClients")
    private Clients builtClients;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
    private AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private SessionStore delegatedClientDistributedSessionStore;

    @Autowired
    @Qualifier("argumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Bean
    public CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator() {
        CookieGenerationContext context = CookieGenerationContext.EMPTY; // TODO ???
        return new CookieRetrievingCookieGenerator(context);
    }

    @Bean
    public CasWebflowConfigurer loginFlowConfigurer() {
        return new LoginFlowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties, ticketRegistrySupport, builtClients);
    }
    @Bean
    public CasWebflowConfigurer logoutFlowConfigurer() {
        return new LogoutFlowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties);
    }

    @Bean
    public DelegatedClientWebflowManager delegatedClientWebflowManager() {
        return new DelegatedClientWebflowManager(ticketRegistry,
                ticketFactory,
                casProperties,
                authenticationRequestServiceSelectionStrategies,
                argumentExtractor
        );
    }

    @Bean
    public Action delegatedAuthenticationAction() {
        return new DelegatedClientAuthenticationAction(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                builtClients,
                servicesManager,
                registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
                delegatedClientWebflowManager(),
                authenticationSystemSupport,
                casProperties,
                authenticationRequestServiceSelectionStrategies,
                centralAuthenticationService,
                webflowSingleSignOnParticipationStrategy,
                delegatedClientDistributedSessionStore,
                CollectionUtils.wrap(argumentExtractor))
        {
            @Override
            public Event doExecute(RequestContext context) {
                try {
                    return super.doExecute(context);
                } catch (Exception e) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
            }
            /* TODO ??
            @Override
            protected Event handleException(JEEContext webContext, BaseClient<Credentials, CommonProfile> client, Exception e) {
                if (e instanceof HttpAction) {
                    return handleLogout((HttpAction) e, RequestContextHolder.getRequestContext(), webContext);
                }
                return super.handleException(webContext, client, e);
            }

            private Event handleLogout(HttpAction httpAction, RequestContext requestContext, JEEContext webContext) {
                switch (httpAction.getCode()) {
                    case HttpConstants.TEMPORARY_REDIRECT:
                        String redirectUrl = webContext.getResponse().getHeader(HttpConstants.LOCATION_HEADER);
                        WebUtils.putLogoutRedirectUrl(requestContext, redirectUrl);
                        return result(TRANSITION_ID_LOGOUT);
                    default:
                        throw new IllegalArgumentException("Unhandled logout response code: " + httpAction.getCode());
                }
            }
*/

    };
    }


    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(loginFlowConfigurer());
        plan.registerWebflowConfigurer(logoutFlowConfigurer());
    }

}
