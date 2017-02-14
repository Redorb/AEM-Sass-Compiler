package com.redorb.aemfuturecompiler;

import com.redorb.aemfuturecompiler.compilers.EcmaScriptNextCompiler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EcmaScriptNextCompilerTest {
    @Test
    public void testBasicCompile() {
        EcmaScriptNextCompiler es6 = EcmaScriptNextCompiler.getInstance();
        try {
            assertEquals("(function(){console.log(\"hello\")});",
                    es6.compileFile("src/test/java/com/redorb/testingResources/basic_test.es6"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }
    @Test
    public void testClassAdvCompile() {
        EcmaScriptNextCompiler es6 = EcmaScriptNextCompiler.getInstance();
        try {
            assertEquals("var c=\"undefined\"!=typeof window&&window===this?this:\"undefined\"!=typeof global&&null!=global?global:this;function e(a,b){function h(){}h.prototype=b.prototype;a.prototype=new h;a.prototype.constructor=a;for(var d in b)if(Object.defineProperties){var k=Object.getOwnPropertyDescriptor(b,d);k&&Object.defineProperty(a,d,k)}else a[d]=b[d]}function f(a,b){this.name=\"Polygon\";this.height=a;this.width=b}f.prototype.a=function(){ChromeSamples.log(\"Hi, I am a \",this.name+\".\")};f.prototype.c=function(){ChromeSamples.log('\"Polygon\" is derived from the Greek polus (many) and gonia (angle).')};\n" +
                            "var g=new f(300,400);g.a();ChromeSamples.log(\"The width of this polygon is \"+g.width);ChromeSamples.log(\"Hi. I was created with a Class expression. My name is \"+function(){}.name);function l(a){f.call(this,a,a);this.name=\"Square\"}e(l,f);c.Object.defineProperties(l.prototype,{b:{configurable:!0,enumerable:!0,get:function(){return this.height*this.width},set:function(a){this.b=a}}});var m=new l(5);m.a();ChromeSamples.log(\"The area of this square is \"+m.b);\n" +
                            "function n(a,b){f.call(this,a,b);this.name=\"Rectangle\"}e(n,f);n.prototype.a=function(){ChromeSamples.log(\"Sup! My name is \",this.name+\".\");f.prototype.c.call(this)};(new n(50,60)).a();ChromeSamples.log(18);ChromeSamples.log(81);",
                    es6.compileFile("src/test/java/com/redorb/testingResources/class_adv_test.es6"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }
}
