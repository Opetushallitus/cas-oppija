package fi.vm.sade.cas.oppija.configuration.action;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REDIRECT_TO_VALTUUDET;

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
        Boolean isValtuudetEnabled = REDIRECT_TO_VALTUUDET;

        if (!context.getExternalContext().getRequestParameterMap().isEmpty() && context.getExternalContext().getRequestParameterMap().contains("valtuudet")) {
            isValtuudetEnabled = context.getExternalContext().getRequestParameterMap().getBoolean("valtuudet");
            loginFlow.getAttributes().put("valtuudet", isValtuudetEnabled);
        } else if (context.getExternalContext().getRequestParameterMap().contains("service") && context.getExternalContext().getRequestParameterMap().get("service").contains("initsession")) {
            loginFlow.getAttributes().put("valtuudet", isValtuudetEnabled);
        }
        return success();
    }
}
