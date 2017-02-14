package com.redorb.aemfuturecompiler.impl;

import com.adobe.granite.ui.clientlibs.script.CompilerContext;
import com.adobe.granite.ui.clientlibs.script.ScriptCompiler;
import com.adobe.granite.ui.clientlibs.script.ScriptResource;
import com.adobe.granite.ui.clientlibs.script.Utils;
import com.redorb.aemfuturecompiler.compilers.EcmaScriptNextCompiler;
import com.redorb.aemfuturecompiler.compilers.SassCompiler;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@Service({
    ScriptCompiler.class
})
public class AEMESNextCompilerService implements ScriptCompiler {
    private static final Logger log = LoggerFactory.getLogger(AEMESNextCompilerService.class);
    private SassCompiler sassCompiler = SassCompiler.getInstance();
    private Set<String> supportedFilesTypes = new HashSet<>(
            Arrays.asList("js", ".js", "es6", ".es6", "es7", ".es7"));
    private boolean includeSourceMarkers;

    public AEMESNextCompilerService() {
    }

    public boolean isIncludeSourceMarkers()
    {
        return this.includeSourceMarkers;
    }

    public void setIncludeSourceMarkers(boolean includeSourceMarkers)
    {
        this.includeSourceMarkers = includeSourceMarkers;
    }

    public String getName() {
        return "js";
    }

    public String getMimeType() {
        return "text/javascript";
    }

    public boolean handles(String s) {
        return supportedFilesTypes.contains(s);
    }

    public String getOutputExtension() {
        return "js";
    }

    public void compile(Collection <ScriptResource> src, Writer dst, CompilerContext ctx) throws IOException {
        src.parallelStream().forEach(res -> AEMESNextCompilerService.compileScriptResource(res, dst, this.includeSourceMarkers));
    }

    // TODO: A lot of this is redundant with SassCompiler, make util class method that takes in compiler and extend them from common interface.
    private static void compileScriptResource(ScriptResource res, Writer dst, Boolean includeSourceMarkers) {
        long t0 = System.currentTimeMillis();
        log.info("Compiling {}...", res.getName());

        String fileSrc = retrieveInputString(res);
        if (log.isDebugEnabled()) {
            log.debug("js source: {}", fileSrc);
        }

        try {
            EcmaScriptNextCompiler compiler = EcmaScriptNextCompiler.getInstance();
            long t1 = System.currentTimeMillis();
            log.info("Setup js compiler environment in {}ms", t1 - t0);

            // Compile css and rewrite paths in files to reference css.
            // TODO: Write own compiler method to do a path rewrite.
            String css = compiler.compileString(fileSrc);
            long t2 = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("compiled output is: {}", css);
            }
            if (includeSourceMarkers)
            {
                dst.write(String.format("/*---------------------------------------------------------------< %s >---*/%n", Text.getName(res.getName())));
                dst.write(String.format("/* %s (%dms) */%n", new Object[] { res.getName(), Long.valueOf(t2 - t1) }));
            }
            dst.write(css);
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dumpError(Writer out, String name, String message, String jsSrc) throws IOException {
        log.error("failed to compile js {}: {}", name, message);
        out.write("/*****************************************************\n");
        out.write("JavaScript compilation failed due a JavaScript error!\n\n");
        out.write("Input: " + name + "\n");
        out.write("Error: " + message + "\n\n");
        out.write("(uncompiled JavaScript src is included below)\n");
        out.write("*****************************************************/\n");
        out.write(jsSrc);
    }

    private static String retrieveInputString(ScriptResource r) {
        String src = "";

        try (Reader in = r.getReader()) {
            src = IOUtils.toString( in );
            src = src.replace("\r", "");
        } catch (IOException e) {
            log.error("failed to compile js {}", e.getMessage());
        }

        return src;
    }

    public static class ResourceLoader {
        private final CompilerContext ctx;

        public ResourceLoader(CompilerContext ctx) {
            this.ctx = ctx;
        }

        public String load(String path) throws FileNotFoundException {
            if (!path.startsWith("/")) {
                AEMESNextCompilerService.log.warn("Only absolute paths supported in @import statements: {}", path);
                throw new FileNotFoundException(path);
            }
            String name = Text.getName(path);
            String ext = Text.getName(name, '.');
            if (ext.length() == 0) {
                path = path + ".js";
            }
            try {
                ScriptResource r = this.ctx.getResourceProvider().getResource(path);
                if (r == null) {
                    throw new FileNotFoundException(path);
                }
                this.ctx.getDependencies().add(path);
                return AEMESNextCompilerService.retrieveInputString(r);
            } catch (Exception e) {
                AEMESNextCompilerService.log.error("Error while loading @import resource {}", path, e);
                throw new FileNotFoundException(path);
            }
        }
    }
}