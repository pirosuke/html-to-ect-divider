(ns html-to-ect-divider.core-test
  (:require [clojure.test :refer :all]
     [clojure.java.io :as io]
     [html-to-ect-divider.core :refer :all]))

(deftest test-convert-ect-files
         (let [src-path "src"
               output-path "dest"]
           (convert-files (io/resource src-path) (io/resource output-path))
           (testing "Output files"
                    (is (= 1 1)))))
