package com.redorb.aemfuturecompiler.impl;

import com.adobe.granite.ui.clientlibs.script.CompilerContext;
import com.adobe.granite.ui.clientlibs.script.ScriptCompiler;
import com.adobe.granite.ui.clientlibs.script.ScriptResource;
import com.redorb.aemfuturecompiler.compilers.SassCompiler;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public AEMESNextCompilerService() {
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
        src.parallelStream().forEach(AEMESNextCompilerService::compileScriptResource);
    }

    private static void compileScriptResource(ScriptResource res) {
        log.info("Compiling {}...", res.getName());

        String fileSrc = retrieveInputString(res);
        if (log.isDebugEnabled()) {
            log.debug("js source: {}", fileSrc);
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