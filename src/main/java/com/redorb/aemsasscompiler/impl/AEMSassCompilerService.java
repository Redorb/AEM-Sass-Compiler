package com.redorb.aemsasscompiler.impl;

import com.adobe.granite.ui.clientlibs.script.CompilerContext;
import com.adobe.granite.ui.clientlibs.script.ScriptCompiler;
import com.adobe.granite.ui.clientlibs.script.ScriptResource;
import com.redorb.aemsasscompiler.SassCompiler;
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
import java.util.Collection;

@Component
@Service({
    ScriptCompiler.class
})
public class AEMSassCompilerService implements ScriptCompiler {
    private static final Logger log = LoggerFactory.getLogger(AEMSassCompilerService.class);
    private SassCompiler sassCompiler = SassCompiler.getInstance();

    public AEMSassCompilerService() {
    }

    public String getName() {
        return "scss";
    }

    public String getMimeType() {
        return "text/css";
    }

    public boolean handles(String s) {
        return ("scss".equals(s)) || (".scss".equals(s));
    }

    public String getOutputExtension() {
        return "css";
    }

    public void compile(Collection <ScriptResource> src, Writer dst, CompilerContext ctx) throws IOException {
        src.stream().forEach(AEMSassCompilerService::compileScriptResource);
    }

    private static void compileScriptResource(ScriptResource res) {
        log.info("Compiling {}...", res.getName());

        String fileSrc = retrieveInputString(res);
        if (log.isDebugEnabled()) {
            log.debug("less source: {}", fileSrc);
        }
    }

    private void dumpError(Writer out, String name, String message, String lessSrc) throws IOException {
        log.error("failed to compile sass {}: {}", name, message);
        out.write("/*****************************************************\n");
        out.write("Sass compilation failed due a JavaScript error!\n\n");
        out.write("Input: " + name + "\n");
        out.write("Error: " + message + "\n\n");
        out.write("(uncompiled Sass src is included below)\n");
        out.write("*****************************************************/\n");
        out.write(lessSrc);
    }

    private static String retrieveInputString(ScriptResource r) {
        String src = "";

        try (Reader in = r.getReader()) {
            src = IOUtils.toString( in );
            src = src.replace("\r", "");
        } catch (IOException e) {
            log.error("failed to compile sass {}", e.getMessage());
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
                path = path + ".less";
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