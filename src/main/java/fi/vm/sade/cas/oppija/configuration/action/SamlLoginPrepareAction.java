package fi.vm.sade.cas.oppija.configuration.action;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.VALTUUDET_ENABLED;

/**
 * Get possible valtuudet-parameter from url to enable/disable valtuudet login.
 */
public class SamlLoginPrepareAction extends AbstractAction {
    private Flow loginFlow;

    public SamlLoginPrepareAction(Flow loginFlow) {
        this.loginFlow = loginFlow;
    }

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        Boolean isValtuudetEnabled = VALTUUDET_ENABLED;

        if (!context.getExternalContext().getRequestParameterMap().isEmpty() && context.getExternalContext().getRequestParameterMap().contains("valtuudet")) {
            isValtuudetEnabled = context.getExternalContext().getRequestParameterMap().getBoolean("valtuudet");
            this.loginFlow.getAttributes().put("valtuudet", isValtuudetEnabled);
            return success();
        } else if (context.getExternalContext().getRequestParameterMap().contains("service") && context.getExternalContext().getRequestParameterMap().get("service").contains("initsession")) {
            this.loginFlow.getAttributes().put("valtuudet", isValtuudetEnabled);
            return success();
        } else {
            // NOP - Should be set on initial request only.
            return success();
        }
    }
}
