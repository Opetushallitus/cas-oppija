package fi.vm.sade.cas.oppija;

import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public final class CasOppijaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasOppijaUtils.class);

    private CasOppijaUtils() {
    }

    public static <T> Optional<T> resolveAttribute(Map<String, Object> attributes, String attributeName, Class<T> type) {
        Object attribute = attributes.get(attributeName);
        if (attribute == null) {
            return Optional.empty();
        }
        if (type.isInstance(attribute)) {
            return Optional.of(type.cast(attribute));
        }
        if (attribute instanceof Iterable) {
            Iterable iterable = (Iterable) attribute;
            Iterator iterator = iterable.iterator();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                if (type.isInstance(value)) {
                    return Optional.of(type.cast(value));
                }
            }
        }
        LOGGER.warn("Cannot parse {} to {} (type={}, value={})", attributeName, type, attribute.getClass(), attribute);
        return Optional.empty();
    }

    public static boolean sessionContainsAttribute(RequestContext requestContext, String attributeName) {
        HttpSession session = getSession(requestContext);
        if(session.getAttribute(attributeName) != null) return true;
        return false;
    }

    public static Boolean getSessionAttributeBoolean(RequestContext requestContext, String attributeName) {
        HttpSession session = getSession(requestContext);
        Object attribute = session.getAttribute(attributeName);
        if(attribute == null) return null;
        return (Boolean) attribute;
    }

    public static void setSessionAttribute(RequestContext requestContext, String attributeName, Object attribute) {
        HttpSession session = getSession(requestContext);
        session.setAttribute(attributeName, attribute);
    }

    public static void removeSessionAttribute(RequestContext requestContext, String attributeName) {
        HttpSession session = getSession(requestContext);
        session.removeAttribute(attributeName);
    }

    private static HttpSession getSession(RequestContext requestContext) {
        HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        HttpSession session = request.getSession();
        return session;
    }

}
