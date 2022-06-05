package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.cas.oppija.configuration.action.Pac4jClientProvider;
import lombok.val;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.exceptions.SAMLException;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.PARAMETER_SAML_RELAY_STATE;
import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

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
                HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                JEEContext context = new JEEContext(request, response);
                if (isLogoutRequest(request)) {
                    try {
                        SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
                        SAML2Client client = (SAML2Client) clientProvider.getClient(profile);
                        val samlContext = client.getContextProvider().buildContext(client, context, sessionStore);
                        client.getLogoutProfileHandler().receive(samlContext);
                        } catch (final HttpAction action) {
                            return handleLogout(action, requestContext);
                        }
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

           private Event handleLogout(HttpAction httpAction, RequestContext requestContext) {
                if (httpAction.getCode() == 200) {
                    String redirectUrl = requestContext.getRequestParameters().get(PARAMETER_SAML_RELAY_STATE);
                    WebUtils.putLogoutRedirectUrl(requestContext, redirectUrl);
                    return result(TRANSITION_ID_LOGOUT);
                }
                throw new IllegalArgumentException("Unhandled logout response code: " + httpAction.getCode());
            }
        };
    }
}
