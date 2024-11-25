package tools.jackson.databind.jsontype.deftyping;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.*;
import tools.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.testutil.DatabindTestUtil;
import tools.jackson.databind.testutil.NoCheckSubTypeValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultTypeAbstractMapping3235Test extends DatabindTestUtil
{
    // [databind#3235]
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
    static class Parent { }
    static class Child extends Parent { }

    static abstract class AbstractParentWithoutDefault {}

    static class ChildOfParentWithoutDefault extends AbstractParentWithoutDefault {
        public Map<String,String> mapField;
        public Parent objectField;
    }

    // [databind#3235]
    @Test
    public void testForAbstractTypeMapping() throws Exception
    {
        // [databind#3235]
        ObjectMapper mapper3235 = jsonMapperBuilder()
                .enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
                .addModule(new SimpleModule()
                        .addAbstractTypeMapping(AbstractParentWithoutDefault.class, ChildOfParentWithoutDefault.class)
                        .addAbstractTypeMapping(Map.class, TreeMap.class)
                        .addAbstractTypeMapping(List.class, LinkedList.class)
                )
                .registerSubtypes(TreeMap.class, LinkedList.class, ChildOfParentWithoutDefault.class)
                .setDefaultTyping(
                        new DefaultTypeResolverBuilder( NoCheckSubTypeValidator.instance,
                                DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY,
                                JsonTypeInfo.Id.CLASS, "foo")
                )
                .build();
        String doc = a2q(
                "{" +
                "  'mapField': {" +
                "    'a':'a'" +
                "  }, " +
                "  'objectField': {}" +
                "}");
        Object o = mapper3235.readValue(doc, AbstractParentWithoutDefault.class);
        assertEquals(o.getClass(), ChildOfParentWithoutDefault.class);
        ChildOfParentWithoutDefault ot = (ChildOfParentWithoutDefault) o;
        assertEquals(ot.mapField.getClass(), TreeMap.class);
        assertEquals(ot.objectField.getClass(), Parent.class);
    }
}
