package tools.jackson.databind.util;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.util.JsonParserSequence;
import tools.jackson.databind.testutil.DatabindTestUtil;

public class JsonParserSequenceTest extends DatabindTestUtil
{
    /**
     * Verifies fix for [core#372]
     */
    @SuppressWarnings("resource")
    @Test
    public void testJsonParserSequenceOverridesSkipChildren() throws Exception
    {
        // Create parser from TokenBuffer containing an incomplete JSON object
        TokenBuffer buf1 = TokenBuffer.forGeneration();
        buf1.writeStartObject();
        buf1.writeName("foo");
        buf1.writeStartObject();
        JsonParser parser1 = buf1.asParser(ObjectReadContext.empty());

        // Create parser from second TokenBuffer that completes the object started by the first buffer
        TokenBuffer buf2 = TokenBuffer.forGeneration();
        buf2.writeEndObject();
        buf2.writeEndObject();
        JsonParser parser2 = buf2.asParser(ObjectReadContext.empty());

        // Create sequence of both parsers and verify tokens
        JsonParser parserSequence = JsonParserSequence.createFlattened(false, parser1, parser2);
        assertToken(JsonToken.START_OBJECT, parserSequence.nextToken());
        assertToken(JsonToken.PROPERTY_NAME, parserSequence.nextToken());
        assertToken(JsonToken.START_OBJECT, parserSequence.nextToken());

        // Skip children of current token. JsonParserSequence's overridden version should switch to the next parser
        // in the sequence
        parserSequence.skipChildren();

        // Verify last token
        assertToken(JsonToken.END_OBJECT, parserSequence.nextToken());
    }
}
