package com.fasterxml.jackson.databind.records;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.PropertyAccessor;

import tools.jackson.databind.BaseMapTest;
import tools.jackson.databind.ObjectMapper;

public class RecordIgnoreNonAccessorGetterTest extends BaseMapTest {

    // [databind#3628]
    interface InterfaceWithGetter {

        String getId();

        String getName();
    }

    @JsonPropertyOrder({"id", "name", "count"}) // easier to assert when JSON field ordering is always the same
    record RecordWithInterfaceWithGetter(String name) implements InterfaceWithGetter {

        @Override
        public String getId() {
            return "ID:" + name;
        }

        @Override
        public String getName() {
            return name;
        }

        // [databind#3895]
        public int getCount() {
            return 999;
        }
    }

    public void testSerializeIgnoreInterfaceGetter_WithoutUsingVisibilityConfig() throws Exception {
        final ObjectMapper MAPPER = newJsonMapper();
        String json = MAPPER.writeValueAsString(new RecordWithInterfaceWithGetter("Bob"));

        assertEquals("{\"id\":\"ID:Bob\",\"name\":\"Bob\",\"count\":999}", json);
    }

    public void testSerializeIgnoreInterfaceGetter_UsingVisibilityConfig() throws Exception {
        final ObjectMapper MAPPER = jsonMapperBuilder()
                .changeDefaultVisibility(vc ->
                    vc.withVisibility(PropertyAccessor.GETTER, Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                )
                .build();

        String json = MAPPER.writeValueAsString(new RecordWithInterfaceWithGetter("Bob"));

        assertEquals("{\"name\":\"Bob\"}", json);
    }
}