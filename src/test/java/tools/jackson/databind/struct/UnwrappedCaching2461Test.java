package tools.jackson.databind.struct;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnwrappedCaching2461Test extends DatabindTestUtil {
    // [databind#2461]
    static class Base {
        public String id;

        Base(String id) {
            this.id = id;
        }
    }

    static class InnerContainer {
        @JsonUnwrapped(prefix = "base.")
        public Base base;

        InnerContainer(Base base) {
            this.base = base;
        }
    }

    static class OuterContainer {
        @JsonUnwrapped(prefix = "container.")
        public InnerContainer container;

        OuterContainer(InnerContainer container) {
            this.container = container;
        }
    }

    // [databind#2461]
    @Test
    void unwrappedCaching() throws Exception {
        final InnerContainer inner = new InnerContainer(new Base("12345"));
        final OuterContainer outer = new OuterContainer(inner);

        final String EXP_INNER = "{\"base.id\":\"12345\"}";
        final String EXP_OUTER = "{\"container.base.id\":\"12345\"}";

        final ObjectMapper mapperOrder1 = newJsonMapper();
        assertEquals(EXP_OUTER, mapperOrder1.writeValueAsString(outer));
        assertEquals(EXP_INNER, mapperOrder1.writeValueAsString(inner));
        assertEquals(EXP_OUTER, mapperOrder1.writeValueAsString(outer));

        final ObjectMapper mapperOrder2 = newJsonMapper();
        assertEquals(EXP_INNER, mapperOrder2.writeValueAsString(inner));
        //  Used to fail here
        assertEquals(EXP_OUTER, mapperOrder2.writeValueAsString(outer));
    }
}
