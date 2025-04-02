package net.platinumdigitalgroup.jvdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AreteS0ftware
 */
public class TestWriter {

    private final VDFParser parser = new VDFParser();
    private final VDFWriter writer = new VDFWriter();

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

    @Test
    public void testSample() {
        VDFNode node1 = parser.parse(VDF_SAMPLE);
        String result = writer.write(node1, true);
        VDFNode node2 = parser.parse(result);
        //assertStringEquals(VDF_SAMPLE, result);
        assertNodesEquals(node1, node2);
    }

    @Test
    public void testSampleMultimap() {
        VDFNode node1 = parser.parse(VDF_SAMPLE_MULTIMAP);
        String result = writer.write(node1, true);
        VDFNode node2 = parser.parse(result);
        assertNodesEquals(node1, node2);
    }

    private void assertNodesEquals(VDFNode node1, VDFNode node2) {
        for (String key : node1.keySet()) {
            Object[] node1values = node1.get(key);
            Object[] node2values = node2.get(key);
            for (int i = 0; i < node1values.length; i++) {
                Object obj1 = node1values[i];
                Object obj2 = node2values[i];
                if (!(obj1 instanceof VDFNode) && !(obj2 instanceof VDFNode)) {
                    assertEquals(obj1, obj2);
                } else {
                    assertNodesEquals((VDFNode) obj1, (VDFNode) obj2);
                }
            }
        }
    }

}
