package fi.vm.sade.cas.oppija.configuration.action;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Get possible valtuudet-parameter from url to enable/disable valtuudet login.
 */
public class SamlLoginPrepareAction extends AbstractAction {
    public SamlLoginPrepareAction() {
    }

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        Boolean isValtuudetEnabled = true;

        if (!context.getExternalContext().getRequestParameterMap().isEmpty() && context.getExternalContext().getRequestParameterMap().contains("valtuudet")) {
            isValtuudetEnabled = context.getExternalContext().getRequestParameterMap().getBoolean("valtuudet");
        }

        if (!context.getConversationScope().contains("valtuudet")) {
            context.getConversationScope().put("valtuudet", isValtuudetEnabled);
        }
        return success();
    }
}
