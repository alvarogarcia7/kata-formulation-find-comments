(ns find-comments.core
  (:require [clojure.string :as s]))

(def comment-symbols ["#" "//" "--" ";;"])

(defn create-regex
  "Creates regex to search situations when we have one or more quotes with comment symbol inside _and_ trailing comment symbol. 
Example regex (with comment symbol //):
.*[\"'].*//.*[\"'].*?//(.*)"
  [comment-symbol]
  (let [quote-with-anything-surround ".*[\"'].*"]
    (re-pattern
     (str 
      ;; open double quote or quote
      quote-with-anything-surround
      comment-symbol
      ;; closing double quote or quote
      quote-with-anything-surround
      ;; we need first comment symbol to be matched
      ;; so we go to be lazy :)
      "?"
      comment-symbol
      ;; rest of line is the comment part
      "(.*)"))))

(def regexs (map create-regex comment-symbols))

(defn apply-regex
  [regex line]
  (when-let [[_ comment] (re-matches regex line)]
    comment))

(defn find-comment-using-regex
  [line]
  (->> regexs
       (map #(apply-regex % line))
       (drop-while nil?)
       first))

(defn find-comment-in-line
  [comment-symbol line]
  line)

(defn find-comments-in-file 
  "Returns vector of comments."
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (->> rdr
         line-seq
         ;; (map find-comment-in-line)
         ;; need to materialize our lazy seq
         (into []))))

(defn fix-broken-slash
  "Replaces windows backslash with unix slash."
  [f]
  (s/replace f #"\\" "/"))

(defn find-php-files
  "Returns seq of php file paths."
  [root-path]
  (->> root-path
       ;; let's make some java.io.File with our path
       clojure.java.io/file
       ;; get recursively all files & directories
       file-seq
       ;; we need only files...
       (filter #(.isFile %))
       ;; ... which name ends with ".php"
       (filter #(-> % .getName (.endsWith ".php")))
       ;; and we need relative path to files
       (map #(.getPath %))
       ;; and fix windows slash
       (map fix-broken-slash)
       ))
