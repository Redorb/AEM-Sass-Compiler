package com.redorb.aemsasscompiler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EcmaScriptNextCompilerTest {
    @Test
    public void testStuff() {
        EcmaScriptNextCompiler es6 = EcmaScriptNextCompiler.getInstance();
        try {
            assertEquals("(function(){console.log(\"hello\")});",
                    es6.compileFile("src/test/java/com/redorb/testingResources/test.es6"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }
}
