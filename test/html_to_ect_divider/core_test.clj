(ns html-to-ect-divider.core-test
  (:require [clojure.test :refer :all]
     [clojure.java.io :as io]
     [html-to-ect-divider.core :refer :all]))

(deftest test-convert-ect-files
         (let [src-file-path "html"
               output-file-path "ect"]
           (convert-files (io/resource src-file-path) (io/resource output-file-path))
           (testing "Output files"
                    (is (= 1 1)))))
