package com.redorb.aemfuturecompiler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SassCompilerTest {

    @Test
    public void testStuff() {
        SassCompiler sass = SassCompiler.getInstance();
        try {
            assertEquals(".selector {\n  margin: 10px; }\n  .selector .nested {\n    margin: 5px; }\n",
                         sass.compileFile("src/test/java/com/redorb/testingResources/test.sass"));
        } catch (Exception e) {
            fail();
        }
    }
}
