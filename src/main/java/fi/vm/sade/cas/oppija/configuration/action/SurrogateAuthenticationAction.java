package fi.vm.sade.cas.oppija.configuration.action;

import fi.vm.sade.cas.oppija.surrogate.SurrogateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.CODE_PARAMETER_NAME;
import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.TOKEN_PARAMETER_NAME;

@Component
public class SurrogateAuthenticationAction extends AbstractNonInteractiveCredentialsAction {

    public SurrogateAuthenticationAction(CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                         CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                         AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Credential constructCredentialsFromRequest(RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        return new SurrogateCredential(request.getParameter(TOKEN_PARAMETER_NAME), request.getParameter(CODE_PARAMETER_NAME));
    }
}
