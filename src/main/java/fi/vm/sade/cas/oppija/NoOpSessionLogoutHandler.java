package fi.vm.sade.cas.oppija;

import org.pac4j.core.context.CallContext;
import org.pac4j.core.logout.handler.SessionLogoutHandler;

import java.util.Optional;

public class NoOpSessionLogoutHandler implements SessionLogoutHandler {
    @Override
    public void recordSession(CallContext callContext, String s) {
        // do nothing
    }

    @Override
    public void destroySession(CallContext callContext, String s) {
        // do nothing
    }

    @Override
    public void renewSession(CallContext callContext, String s) {
        // do nothing
    }

    @Override
    public Optional<String> cleanRecord(String s) {
        return Optional.empty();
    }
}
