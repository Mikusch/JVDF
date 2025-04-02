package net.platinumdigitalgroup.jvdf;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Brendan Heinonen
 */
public class TestParser {

    // Maven surefire bug can't include resources when forking is disabled
    private static final String VDF_SAMPLE = """
            "root_node"
            {
                "first_sub_node"
                {
                    "first"     "value1"
                    "second"    "value2"
                }
                "second_sub_node"
                {
                    "third_sub_node"
                    {
                        "fourth"    "value4"
                    }
                    "third"     "value3"
                }
            }""";
    private static final String VDF_SAMPLE_MULTIMAP = """
            "root_node"
            {
                "sub_node"
                {
                    "key"       "value1"
                    "key"       "value2"
                }
                "sub_node"
                {
                    "key"       "value3"
                    "key"       "value4"
                }
            }""";
    private final VDFParser parser = new VDFParser();

    private static final String VDF_SIMPLE_TEST = "key value";
    private static final String VDF_SIMPLE_TEST_RESULT = "value";

    @Test
    public void testSimple() {
        assertEquals(VDF_SIMPLE_TEST_RESULT, parser.parse(VDF_SIMPLE_TEST).getString("key"));
    }

    private static final String VDF_QUOTES_TEST = "\"key with space\" \"value with space\"";
    private static final String VDF_QUOTES_TEST_RESULT = "value with space";

    @Test
    public void testQuotes() {
        assertEquals(VDF_QUOTES_TEST_RESULT, parser.parse(VDF_QUOTES_TEST).getString("key with space"));
    }

    private static final String VDF_ESCAPE_TEST = "\"key with \\\"\" \"value with \\\" \" \"newline\" \"val\\n\\nue\"";
    private static final String VDF_ESCAPE_TEST_RESULT = "value with \" ";

    @Test
    public void testEscape() {
        VDFNode node = parser.parse(VDF_ESCAPE_TEST);
        assertEquals(VDF_ESCAPE_TEST_RESULT, node.getString("key with \""));
        assertEquals("val\n\nue", node.getString("newline"));
    }

    private static final String VDF_NULLKV_TEST = "\"key\" \"\" \"spacer\" \"spacer\" \"\" \"value\"";

    @Test
    public void testNullKeyValue() {
        VDFNode node = parser.parse(VDF_NULLKV_TEST);
        assertEquals("", node.getString("key"));
        assertEquals("value", node.getString(""));
    }

    private static final String VDF_SUBSEQUENTKV_TEST = "\"key\"\"value\"";

    @Test
    public void testSubsequentKeyValue() {
        VDFNode node = parser.parse(VDF_SUBSEQUENTKV_TEST);
        assertEquals("value", node.getString("key"));
    }


    private static final String VDF_UNDERFLOW_TEST = "root_node { child_node { key value }";

    @Test
    public void testUnderflow() {
        assertThrows(VDFParseException.class, () -> parser.parse(VDF_UNDERFLOW_TEST));
    }

    private static final String VDF_OVERFLOW_TEST = "root_node { child_node { key value } } }";

    @Test
    public void testOverflow() {
        assertThrows(VDFParseException.class, () -> parser.parse(VDF_OVERFLOW_TEST));
    }

    private static final String VDF_CHILD_TEST = "root { child { key value } }";
    private static final String VDF_CHILD_TEST_RESULT = "value";

    @Test
    public void testChild() {
        assertEquals(VDF_CHILD_TEST_RESULT, parser.parse(VDF_CHILD_TEST)
                .getSubNode("root")
                .getSubNode("child")
                .getString("key"));
    }

    @Test
    public void testSample() {
        VDFNode root = parser.parse(VDF_SAMPLE);

        assertEquals(VDFNode.class, root.getSubNode("root_node").getClass());
        assertEquals("value1", root
                .getSubNode("root_node")
                .getSubNode("first_sub_node")
                .getString("first"));
        assertEquals("value2", root
                .getSubNode("root_node")
                .getSubNode("first_sub_node")
                .getString("second"));
        assertEquals("value3", root
                .getSubNode("root_node")
                .getSubNode("second_sub_node")
                .getString("third"));
        assertEquals("value4", root
                .getSubNode("root_node")
                .getSubNode("second_sub_node")
                .getSubNode("third_sub_node")
                .getString("fourth"));
    }

    @Test
    public void testDefaultValue() {
        VDFNode root = parser.parse(VDF_SAMPLE);
        assertEquals("not_existing", root
                .getString("this_key_does_not_exist", "not_existing"));
        assertEquals(1, root
                .getInt("this_key_does_not_exist", 1));
        assertEquals(0.123f, root
                .getFloat("this_key_does_not_exist", 0.123f), 0f);
        assertEquals(Long.MAX_VALUE, root
                .getLong("this_key_does_not_exist", Long.MAX_VALUE), 0f);
        assertEquals(Color.CYAN, root
                .getColor("this_key_does_not_exist", Color.CYAN));
    }

    @Test
    public void testMultimap() {
        VDFNode root = parser.parse(VDF_SAMPLE_MULTIMAP);

        assertEquals(2, root.getSubNode("root_node").values("sub_node"));
        assertEquals("value1", root
                .getSubNode("root_node")
                .getSubNode("sub_node", 0)
                .getString("key"));
        assertEquals("value4", root
                .getSubNode("root_node")
                .getSubNode("sub_node", 1)
                .getString("key", 1));
    }

    @Test
    public void testReduceMultimap() {
        VDFNode root = parser.parse(VDF_SAMPLE_MULTIMAP).reduce();

        assertEquals("value1", root
                .getSubNode("root_node")
                .getSubNode("sub_node")
                .getString("key", 0));

        assertEquals("value4", root
                .getSubNode("root_node")
                .getSubNode("sub_node")
                .getString("key", 3));
    }

}
