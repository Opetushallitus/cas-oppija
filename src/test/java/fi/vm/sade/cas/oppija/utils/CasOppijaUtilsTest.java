package fi.vm.sade.cas.oppija.utils;

import fi.vm.sade.cas.oppija.CasOppijaUtils;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CasOppijaUtilsTest {

    @Test
    public void resolveAttributeWithEmptyAttributesShouldReturnEmpty() {
        Map<String, List<Object>> attributes = Map.of();

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

    @Test
    public void resolveAttributeWithCorrectTypeShouldReturnValue() {
        Map<String, List<Object>> attributes = Map.of("key1", List.of("value1"), "key2", List.of("value2"));

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.of("value1"), attribute);
    }

    @Test
    public void resolveAttributeWithIncorrectTypeShouldReturnEmpty() {
        Map<String, List<Object>>  attributes = Map.of("key1", List.of(1), "key2", List.of(2));

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

    @Test
    public void resolveAttributeWithEmptyListShouldReturnEmpty() {
        Map<String, List<Object>> attributes = Map.of("key1", new ArrayList<>());

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

    @Test
    public void resolveAttributeWithCorrectTypeInListShouldReturnValue() {
        Map<String, List<Object>> attributes = Map.of("key1", List.of("value11", "value12"), "key2", List.of("value21", "value22"));

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.of("value11"), attribute);
    }

    @Test
    public void resolveAttributeWithIncorrectTypeInListShouldReturnEmpty() {
        Map<String, List<Object>> attributes = Map.of("key1", List.of(11, 12), "key2", List.of(21, 22));

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

}
