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
        Boolean valtuudetEnabled = true;

        if (!context.getExternalContext().getRequestParameterMap().isEmpty() && context.getExternalContext().getRequestParameterMap().contains("valtuudet")) {
            valtuudetEnabled = context.getExternalContext().getRequestParameterMap().getBoolean("valtuudet");
        }

        if (!context.getFlowScope().contains("valtuudet")) {
            context.getFlowScope().put("valtuudet", valtuudetEnabled);
        }
        return success();
    }
}
