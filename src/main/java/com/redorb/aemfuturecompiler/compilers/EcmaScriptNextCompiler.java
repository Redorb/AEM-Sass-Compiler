package com.redorb.aemfuturecompiler.compilers;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EcmaScriptNextCompiler {
    private static final Logger log = LoggerFactory.getLogger(SassCompiler.class);

    public EcmaScriptNextCompiler() {}

    public String compileFile(String fileURI) throws IOException {
        List<SourceFile> source = new ArrayList<>();
        source.add(SourceFile.fromFile(fileURI));
        return compile(source);
    }

    public String compileString(String src) throws IOException {
        List<SourceFile> source = new ArrayList<>();
        source.add(SourceFile.fromCode("toEval.js", src));
        return compile(source);
    }

    private String compile(List<SourceFile> source) throws IOException {
        Compiler compiler = new Compiler();

        // Steal the builtin externs from the command line runner defaults.
        List<SourceFile> extern = CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
        CompilerOptions options = new CompilerOptions();

        // Add our max language in, in this case ESNext.
        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/New_in_JavaScript/ECMAScript_Next_support_in_Mozilla
        // Somethings however can't be transpiled to ES2015 such as built in type subclassing and async.
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);

        // Add our language out, which is ES5 to support IE9+
        // https://kangax.github.io/compat-table/es5/
        options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);

        options.setStrictModeInput(false);

        // Advanced mode is used here, but additional options could be set too.
        CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        compiler.compile(extern, source, options);
        return compiler.toSource();
    }
}
