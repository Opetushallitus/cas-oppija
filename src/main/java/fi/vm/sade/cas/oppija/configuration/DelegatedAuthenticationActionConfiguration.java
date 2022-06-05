package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.cas.oppija.configuration.action.Pac4jClientProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.exceptions.SAMLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_STOP;

@Configuration
public class DelegatedAuthenticationActionConfiguration {
    // override default delegatedAuthenticationAction to automatically logout on error
    static final String TRANSITION_ID_LOGOUT = "logout";
    @Autowired
    private Pac4jClientProvider clientProvider;
    @Autowired
    private SessionStore sessionStore;

    @Bean
    public Action delegatedAuthenticationAction(
            final DelegatedClientAuthenticationConfigurationContext context,
            final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager
    ) {
        return new DelegatedClientAuthenticationAction(context, delegatedClientAuthenticationWebflowManager ) {
            @Override
            public Event doExecute(RequestContext requestContext) {
                HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                if (isLogoutRequest(request)) {
                    return result(TRANSITION_ID_STOP);
                }
                return super.doExecute(requestContext);
            }

            @Override
            protected Event stopWebflow(Exception e, RequestContext requestContext) {
                if (e instanceof SAMLException) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
                return super.stopWebflow(e, requestContext);
            }

           /*private Event handleLogout(HttpAction httpAction, RequestContext requestContext) {
                if (httpAction.getCode() == 200) {
                    String redirectUrl = requestContext.getRequestParameters().get(PARAMETER_SAML_RELAY_STATE);
                    WebUtils.putLogoutRedirectUrl(requestContext, redirectUrl);
                    return result(TRANSITION_ID_LOGOUT);
                }
                throw new IllegalArgumentException("Unhandled logout response code: " + httpAction.getCode());
            }*/
        };
    }
}
