package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Objects;

public class StoreServiceParamAction extends AbstractServiceParamAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreServiceParamAction.class);
    private final CasConfigurationProperties casProperties;

    public StoreServiceParamAction(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    protected Event doExecuteInternal(RequestContext requestContext) {
        LOGGER.info("Executing {}", getClass().getSimpleName());
        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        var service = casProperties.getLogout().getRedirectParameter().stream()
                .map(request::getParameter)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (service != null) {
            LOGGER.info("Setting service redirect cookie to value: {}", service);
            setServiceRedirectCookie(response, service);
        } else {
            service = casProperties.getLogout().getRedirectUrl();
            LOGGER.info("Setting service redirect cookie to default value: {}", service);
            setServiceRedirectCookie(response, service);
        }
        return null;
    }

}
