package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.SAML2Profile;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

/**
 * SAML logout action. Expects SAML profile to be in request scope ({@link SamlLogoutPrepareAction}).
 * Implementation is mostly derived from DelegatedAuthenticationSAML2ClientLogoutAction.
 */
public class SamlLogoutExecuteAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlLogoutExecuteAction.class);

    private final Clients clients;

    public SamlLogoutExecuteAction(Clients clients) {
        this.clients = clients;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            var context = Pac4jUtils.getPac4jJ2EContext(request, response);

            Client<?, ?> client;
            SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
            try {
                var currentClientName = profile == null ? null : profile.getClientName();
                client = currentClientName == null ? null : clients.findClient(currentClientName);
            } catch (final TechnicalException e) {
                LOGGER.info("No SAML2 client found: " + e.getMessage(), e);
                client = null;
            }
            if (client instanceof SAML2Client) {
                var saml2Client = (SAML2Client) client;
                LOGGER.debug("Located SAML2 client [{}]", saml2Client);
                var service = request.getParameter("service");
                if (service != null) {
                    String requestUrl = String.join("?", request.getRequestURL(), request.getQueryString());
                    context.getSessionStore().set(context, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, requestUrl);
                }
                var action = saml2Client.getLogoutAction(context, profile, null);
                LOGGER.info("Preparing logout message to send is [{}]", action.getLocation());
                return handleLogout(action, requestContext);
            } else {
                LOGGER.info("The current client is not a SAML2 client or it cannot be found at all, no logout action will be executed.");
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    protected Event handleLogout(RedirectAction action, RequestContext context) {
        LOGGER.info("LOGOUT | Service: {} | Action: {}", context.getExternalContext().getRequestParameterMap().get("service"), action.getLocation());
        switch (action.getType()) {
            case REDIRECT:
                WebUtils.putLogoutRedirectUrl(context, action.getLocation());
                return null;
            default:
                throw new IllegalArgumentException("Unhandled logout request code: " + action.getType());
        }
    }

}
