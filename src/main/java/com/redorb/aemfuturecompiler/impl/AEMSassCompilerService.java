package com.redorb.aemfuturecompiler.impl;

import com.adobe.granite.ui.clientlibs.script.CompilerContext;
import com.adobe.granite.ui.clientlibs.script.ScriptCompiler;
import com.adobe.granite.ui.clientlibs.script.ScriptResource;
import com.adobe.granite.ui.clientlibs.script.Utils;
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
import java.util.*;

@Component
@Service
public class AEMSassCompilerService implements ScriptCompiler {
    private static final Logger log = LoggerFactory.getLogger(AEMSassCompilerService.class);
    private SassCompiler sassCompiler = SassCompiler.getInstance();
    private Set<String> supportedFilesTypes = new HashSet<>(Arrays.asList("scss", ".scss"));
    private boolean includeSourceMarkers;

    public AEMSassCompilerService() {
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
        return "scss";
    }

    public String getMimeType() {
        return "text/css";
    }

    public boolean handles(String s) {
        return supportedFilesTypes.contains(s);
    }

    public String getOutputExtension() {
        return "css";
    }

    public void compile(Collection <ScriptResource> src, Writer dst, CompilerContext ctx) throws IOException {
        ResourceLoader compilerContextRes = new ResourceLoader(ctx);
        src.parallelStream().forEach(res -> AEMSassCompilerService.compileScriptResource(res, dst, ctx, compilerContextRes, this.includeSourceMarkers));
    }

    private static void compileScriptResource(ScriptResource res, Writer dst, CompilerContext ctx, ResourceLoader resourceLoader, Boolean includeSourceMarkers) {
        long t0 = System.currentTimeMillis();
        log.info("Compiling {}...", res.getName());

        String fileSrc = retrieveInputString(res);
        if (log.isDebugEnabled()) {
            log.debug("scss source: {}", fileSrc);
        }

        try {
            SassCompiler compiler = SassCompiler.getInstance();
            long t1 = System.currentTimeMillis();
            log.info("Setup less compiler environment in {}ms", t1 - t0);

            // Compile css and rewrite paths in files to reference css.
            // TODO: Write own compiler method to replace this path rewrite.
            String css = Utils.rewriteUrlsInCss(ctx.getDestinationPath(), res.getName(), compiler.compileString(fileSrc));
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
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dumpError(Writer out, String name, String message, String lessSrc) throws IOException {
        log.error("failed to compile sass {}: {}", name, message);
        out.write("/*****************************************************\n");
        out.write("Scss compilation failed due a JavaScript error!\n\n");
        out.write("Input: " + name + "\n");
        out.write("Error: " + message + "\n\n");
        out.write("(uncompiled Scss src is included below)\n");
        out.write("*****************************************************/\n");
        out.write(lessSrc);
    }

    private static String retrieveInputString(ScriptResource r) {
        String src = "";

        try (Reader in = r.getReader()) {
            src = IOUtils.toString( in );
            src = src.replace("\r", "");
        } catch (IOException e) {
            log.error("failed to compile scss {}", e.getMessage());
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
                AEMSassCompilerService.log.warn("Only absolute paths supported in @import statements: {}", path);
                throw new FileNotFoundException(path);
            }
            String name = Text.getName(path);
            String ext = Text.getName(name, '.');
            if (ext.length() == 0) {
                path = path + ".scss";
            }
            try {
                ScriptResource r = this.ctx.getResourceProvider().getResource(path);
                if (r == null) {
                    throw new FileNotFoundException(path);
                }
                this.ctx.getDependencies().add(path);
                return AEMSassCompilerService.retrieveInputString(r);
            } catch (Exception e) {
                AEMSassCompilerService.log.error("Error while loading @import resource {}", path, e);
                throw new FileNotFoundException(path);
            }
        }
    }
}