(ns html-to-ect-divider.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clojure.java.io :as io]))

(def patterns {:include (re-pattern "(?ms)<!-- (tpl:include=\"([^\"]*)\") -->((?!tpl:include).)*?<!-- /\\1 -->")
               :block (re-pattern "(?ms)<!-- (tpl:block=\"([^\"]*)\") -->(.*?)<!-- /\\1 -->")
               :frame (re-pattern "<!-- tpl:frame=\"([^\"]*)\" -->")
               :text (re-pattern "(?ms)<([a-zA-Z]+)[^>]* tpl:text=\"([^\"]*)\"[^>]*>(.*?)</\\1>")
               :content (re-pattern  "(?ms)<!-- (tpl:content) -->(.*?)<!-- /\\1 -->")})

(defn parse-parts
  "search and replace include parts"
  [src-content pattern-key replacement]
  (loop [matches (re-seq (get patterns pattern-key) src-content)
         matched []
         content src-content]
    (let [replaced-content (clojure.string/replace content (get patterns pattern-key) replacement)]
      (cond (empty? matches) {:matches matched :parsed-content content}
        :else (recur (re-seq (get patterns pattern-key) replaced-content)
               (concat matched (for [match matches]
                                 {:file (str (nth match 2) ".html.ect") :content (first match)}))
               replaced-content)))))

(defn parse-frame-blocks
  "search and replace frame blocks"
  [content-set]
  (let [block-matches (re-seq (:block patterns) (:frame content-set))]
    (loop [block-matches block-matches
           content-set content-set]
      (cond (empty? block-matches) content-set
        :else (let [block-match (first block-matches)
                    block-name (nth block-match 2)
                    block-content (nth block-match 3)
                    frame-content (clojure.string/replace (:frame content-set) block-content (str "<% content \"" block-name "\" %>"))
                    main-content (str "<% block \"" block-name "\" %>" 
                                      block-content
                                      "<% end %>" 
                                      \newline
                                      \newline
                                      (:main content-set))]
                (recur (rest block-matches)
                       {:frame frame-content
                        :main main-content}))))))

(defn parse-frame-texts
  "search and replace frame text parts"
  [content-set]
  (let [text-matches (re-seq (:text patterns) (:frame content-set))]
    (loop [text-matches text-matches
           content-set content-set]
      (cond (empty? text-matches) content-set
        :else (let [text-match (first text-matches)
                    text-name (nth text-match 2)
                    text-content (nth text-match 3)
                    frame-content (clojure.string/replace (:frame content-set) text-content (str "<% content \"" text-name "\" %>"))
                    main-content (str "<% block \"" text-name "\" %>" 
                                      text-content
                                      "<% end %>"
                                      \newline
                                      \newline
                                      (:main content-set))]
                (recur (rest text-matches)
                       {:frame frame-content
                        :main main-content}))))))

(defn parse-frame-contents
  [content-set frame-file frame-matches]
  (let [frame-content (:frame content-set)
        main-content (str "<% extend \"" frame-file "\" %>" 
                          \newline 
                          (:main content-set))]
    {:frame frame-content
     :main main-content}))

(defn parse-frame-parts
  [src-content]
  (let [frame-matches (re-find (:frame patterns) src-content)]
    (cond (empty? frame-matches) {:frame-file "" :frame-content "" :main-content src-content}
      :else
      (let [frame-file (str (nth frame-matches 1) ".html.ect")
            content-matches (re-find (:content patterns) src-content)
            main-content (cond (not-empty content-matches)(nth content-matches 2) :else "")
            frame-content (clojure.string/replace src-content main-content "<% content %>")
            content-set (-> {:frame frame-content :main main-content}
                          (parse-frame-blocks)
                          (parse-frame-texts)
                          (parse-frame-contents frame-file frame-matches))]
        {:frame-file frame-file
         :frame-content (:frame content-set)
         :main-content (:main content-set)}))))

(defn parse-content
  [src-file-name src-content]
  (let [{parts :matches content :parsed-content} (parse-parts src-content :include "<% include \"$2.html.ect\" %>")
        {frame-file :frame-file frame-content :frame-content main-content :main-content} (parse-frame-parts content)]
    (concat parts
            [{:file frame-file
              :content frame-content}
             {:file (str src-file-name ".ect")
              :content main-content}])))

(defn convert-file
  [src-file output-dir-path]
  (let [src-content (slurp src-file)
        file-content-list (parse-content (.getName src-file) src-content)]
    (doseq [fc file-content-list]
      (let [output-file-path (io/file output-dir-path (:file fc))]
        (io/make-parents output-file-path)
        (spit output-file-path (:content fc))))))

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
