package com.redorb.aemsasscompiler;

import com.adobe.granite.ui.clientlibs.script.ScriptCompiler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;

public class SassCompiler {
    private static final Logger log = LoggerFactory.getLogger(SassCompiler.class);
    private static SassCompiler sassCompiler = null;
    private final ScriptEngine scriptEngine = (new ScriptEngineManager()).getEngineByName("nashorn");

    private SassCompiler() {
        this.setupScriptEngine();
    }

    public static SassCompiler getInstance() {
        if (sassCompiler == null) {
            sassCompiler = new SassCompiler();
        }

        return sassCompiler;
    }

    private void setupScriptEngine() {
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
        String[] stringArr = {fileString};
        ((Invocable) scriptEngine).invokeFunction("setSource", fileString);
        return (String) scriptEngine.eval("Sass.compile(source)");
    }
}