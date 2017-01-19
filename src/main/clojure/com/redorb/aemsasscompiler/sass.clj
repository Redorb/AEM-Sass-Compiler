(ns com.redorb.aemsasscompiler.sass
  (:require [clojure.set :refer [rename-keys]]
            [clojure.java.io :as io]
            [clojure.pprint]
            [clojure.osgi.services :as os])
  (:import
    java.io.File
    [java.nio.file FileSystems Paths StandardWatchEventKinds]
    [javax.script Invocable ScriptEngineManager]
    [com.adobe.granite.ui.clientlibs.script.CompilerContext]
    [com.adobe.granite.ui.clientlibs.script.ScriptCompiler]
    [com.adobe.granite.ui.clientlibs.script.ScriptResource]
    [com.adobe.granite.ui.clientlibs.script.ScriptResourceProvider]
    [com.adobe.granite.ui.clientlibs.script.Utils]))

  (defonce engine (let [e (.getEngineByName (ScriptEngineManager.) "nashorn")]
                  (.eval e (io/reader (io/resource "sass.min.js")))
                  (.eval e "var source = '';")
                  (.eval e "function setSource(input) {source = input;};")
                  e))

  (defn compile-file [file]
    (let [source (slurp file)]
      (.invokeFunction (cast Invocable engine) "setSource" (into-array [source]))
      (.eval engine "Sass.compile(source)")))

  (defn find-assets [f ext]
    (when f
      (if (.isDirectory f)
        (->> f
             file-seq
             (filter (fn [file] (-> file .getName (.endsWith ext)))))
        [f])))

  (defn ext-sass->css [file-name]
    (str (subs file-name 0 (.lastIndexOf file-name ".")) ".css"))

  (defn compile-assets [files target]
    (doseq [file files]
      (let [file-name (.getName file)
            _ (println "compiling" file-name)
            compiled  (compile-file file)]
        (if (string? compiled)
          (do
            (println "compiled successfully")
            (spit (str target File/separator (ext-sass->css file-name)) compiled))
          (do
            (println "failed to compile:" file-name)
            (clojure.pprint/pprint compiled))))))
