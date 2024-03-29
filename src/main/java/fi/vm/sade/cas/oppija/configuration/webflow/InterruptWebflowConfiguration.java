package fi.vm.sade.cas.oppija.configuration.webflow;


import fi.vm.sade.cas.oppija.configuration.InterruptInquiryExecutionPlanConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurer;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptAction;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.Expression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.*;
import static java.util.stream.Collectors.toList;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT;


/**
 * This class should include only fixes to default cas interrupt configuration.
 *
 * @see InterruptInquiryExecutionPlanConfiguration actual interrupt configuration
 */

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InterruptWebflowConfiguration implements CasWebflowExecutionPlanConfigurer {

    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    public InterruptWebflowConfiguration(FlowBuilderServices flowBuilderServices,
                                         @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                         ConfigurableApplicationContext applicationContext,
                                         CasConfigurationProperties casProperties,
                                         @Qualifier("ticketGrantingTicketCookieGenerator")
                                  CasCookieBuilder ticketGrantingTicketCookieGenerator
    ) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                /* Redirect endstate to valtuudet is created using the url parameter from flowScope
                and a transition to it from inquireinterruptaction is created. See inquireInterruptAction */
                EndState valtuudetRedirectEndstate = createEndState(getLoginFlow(), STATE_ID_VALTUUDET_INTERRUPT_ACTION);
                Expression expression = createExpression("flowScope.".concat(VALTUUDET_REDIRECT_URL_PARAMETER));
                valtuudetRedirectEndstate.getEntryActionList().add(new ExternalRedirectAction(expression));
                ActionState serviceTicketActionState = getState(getLoginFlow(), STATE_ID_INQUIRE_INTERRUPT, ActionState.class);
                TransitionSet transitions = serviceTicketActionState.getTransitionSet();
                transitions.add(createTransition(TRANSITION_ID_VALTUUDET_INTERRUPT , STATE_ID_VALTUUDET_INTERRUPT_ACTION));
            }
        });
        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // fix interrupt inquirers called twice after successful login (this seems to actually be needed in 6.5 cause the flow is defaultly interrupted multiple times).
                ActionState state = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
                ActionList actions = state.getActionList();
                clear(actions, actions::remove);
                actions.add(super.createEvaluateAction(CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET));
            }
        });

    }

    private static <E, T extends Iterable<E>> void clear(T iterable, Consumer<E> remover) {
        StreamSupport.stream(iterable.spliterator(), false).collect(toList()).forEach(remover::accept);
    }

    @Bean
    public CasWebflowConfigurer interruptWebflowConfigurer() {
        return new InterruptWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            public int getOrder() {
                // This CasWebflowExecutionPlanConfigurer must be run before DelegatedAuthenticationConfiguration to enable
                // surrogate authentication after delegated authentication
                return Ordered.HIGHEST_PRECEDENCE;
            }
        };

    }

    // TODO this should maybe be moved to an own file and moved to actions package.
    // override default inquireInterruptAction to add new interruptRedirect transition
    @Bean
    public InquireInterruptAction inquireInterruptAction(List<InterruptInquirer> interruptInquirers) {
        return new InquireInterruptAction(interruptInquirers, casProperties, ticketGrantingTicketCookieGenerator) {
            @Override
            protected Event doExecute(RequestContext requestContext) {
                Event event = super.doExecute(requestContext);
                if (CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED.equals(event.getId())) {
                    InterruptResponse interruptResponse = InterruptUtils.getInterruptFrom(requestContext);
                    if (interruptResponse.isAutoRedirect() && interruptResponse.getAutoRedirectAfterSeconds() < 0
                            && interruptResponse.getLinks().size() > 0) {
                        requestContext.getFlowScope().put(VALTUUDET_REDIRECT_URL_PARAMETER,
                                interruptResponse.getLinks().values().iterator().next());
                        return result(TRANSITION_ID_VALTUUDET_INTERRUPT);
                    }
                }
                return event;
            }
        };
    }

}

