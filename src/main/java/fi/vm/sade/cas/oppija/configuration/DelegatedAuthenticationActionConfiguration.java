package fi.vm.sade.cas.oppija.configuration;

import lombok.val;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.saml.exceptions.SAMLException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.PARAMETER_SAML_RELAY_STATE;

@Configuration
public class DelegatedAuthenticationActionConfiguration {
    // override default delegatedAuthenticationAction to automatically logout on error
    static final String TRANSITION_ID_LOGOUT = "logout";
    @Bean
    public Action delegatedAuthenticationAction(
            final DelegatedClientAuthenticationConfigurationContext context,
            final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager
    ) {
        return new DelegatedClientAuthenticationAction(context, delegatedClientAuthenticationWebflowManager ) {
            @Override
            public Event doExecute(RequestContext context) {
                HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                if (isLogoutRequest(request)) {
                    try {
                        val webContext = new JEEContext(request, response);
                        val clientName = retrieveClientName(webContext);
                        Service service = populateContextWithService(context, webContext, clientName);
                        val client = findDelegatedClientByName(request, clientName, service);
                        WebUtils.putDelegatedAuthenticationClientName(context, client.getName());
                        populateContextWithClientCredential(client, webContext, context);
                        return null;
                    } catch (HttpAction e) {
                        return handleLogout(e, context, response);
                    }
                }
                return super.doExecute(context);
            }

            @Override
            protected Event stopWebflow(Exception e, RequestContext requestContext) {
                if (e instanceof SAMLException) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
                return super.stopWebflow(e, requestContext);
            }

            private Event handleLogout(HttpAction httpAction, RequestContext requestContext, HttpServletResponse response) {
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
