(ns html-to-ect-divider.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clojure.java.io :as io]))

(defn convert-file
  [src-file output-dir-path]
  (let [src-content (slurp src-file)]
    (println src-content)))

(defn convert-files
  [src-dir-path output-dir-path]
  (let [src-dir (io/file src-dir-path)
        src-file-list (filter #(re-find #"\.html$" (.getName %)) (.listFiles src-dir))]
    (doseq [f src-file-list]
      (println (str "Processing " (.getPath f) "..."))
      (convert-file f output-dir-path))))

(defn create-error-msg
  [error-title errors]
  (str error-title "\n\n" (clojure.string/join \newline errors)))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn -main
  [& args]
  (let [cli-options [["-d" "--dir SRC_DIR_PATH" "Source HTML directory path"]
                     ["-o" "--output OUTPUT_DIR_PATH" "Divided file output directory path"]]
        {:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond errors (exit 1 (create-error-msg "Parameter Error" errors)))
    (convert-files (:dir options) (:output options))))
