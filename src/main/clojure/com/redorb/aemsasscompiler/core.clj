(ns com.redorb.aemsasscompiler.core
  (:require [clojure.osgi.services :as os])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn- bundle-start [context]
  "Register OSGi service"
  (println "Starting OSGi from Clojure")
  (try (os/register-service
         Runnable
         (run [_]
           (println "Helllo")) )
       (catch Exception e
         (println "Unable to register services"))))

(defn- bundle-stop [context]
  (println "bundle-stop is called"))
