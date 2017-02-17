package com.redorb.aemfuturecompiler.compilers;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

// Look to migrate to Libsass for performance.
public class SassCompiler {
    private static final Logger log = LoggerFactory.getLogger(SassCompiler.class);
    private final ScriptEngine scriptEngine = (new ScriptEngineManager()).getEngineByName("nashorn");

    public SassCompiler() {
        this.setupCompiler();
    }

    private void setupCompiler() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("sass.min.js")));
        try {
            scriptEngine.eval(reader);
            scriptEngine.eval("var source = '';");
            scriptEngine.eval("function setSource(input) {source = input;};");
        } catch (ScriptException e) {
            log.error("unexpected error during script engine setup", e);
        }
    }

    public String compileFile(String fileURI) throws IOException, ScriptException, NoSuchMethodException {
        String fileString = FileUtils.readFileToString(new File(fileURI), "UTF-8");
        return compileString(fileString);
    }

    public String compileString(String src) throws ScriptException, NoSuchMethodException {
        ((Invocable) scriptEngine).invokeFunction("setSource", src);
        return (String) scriptEngine.eval("Sass.compile(source)");
    }
}