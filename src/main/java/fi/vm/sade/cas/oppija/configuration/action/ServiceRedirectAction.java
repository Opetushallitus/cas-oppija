package fi.vm.sade.cas.oppija.configuration.action;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
public class ServiceRedirectAction extends AbstractServiceParamAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRedirectAction.class);

    public ServiceRedirectAction() {

    }

    @Override
    protected Event doExecuteInternal(RequestContext context) throws Exception {
        LOGGER.info("Executing {}", getClass().getSimpleName());
        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = new JEEContext(request, response);
        var service = getServiceRedirectCookie(request);
        if (service != null) {
            LOGGER.info("Found service redirect cookie, setting logout redirect url ({}) and clearing the cookie", service);
            clearServiceRedirectCookie(response);
            JEEHttpActionAdapter.INSTANCE.adapt(new FoundAction(service), webContext);
        }
        return success();
    }

}
