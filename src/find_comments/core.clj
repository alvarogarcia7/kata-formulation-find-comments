(ns find-comments.core
  (:require [clojure.string :as s]))

;; Small ubiquitous language dictionary:
;; -------------------------------------------------------------------------
;; | comment        |    | string for which we search
;; | line           |    | line from file, in which we are searching comment
;; | comment symbol | CS | string, after that in line starts comment
;; -------------------------------------------------------------------------

;; Some rules:
;; 1) Quotes and CS can't be same
;; 2) One CS can't be substring of another

(def comment-symbols ["#" "//"])
(def quotes-symbols [\' \"])

;; Helpers

(defn is-quote?
  "Checks that character is a quotation symbol."
  [ch]
  (some #{ch} quotes-symbols))

(defn cut-cs
  "If s starts with comment, cuts CS and returns remain string.
 Otherwise returns nil."
  [s]
  (some #(when (.startsWith s %) (.substring s (count %))) comment-symbols))

(defn find-comment-in-line*
  "quote is nil if parser outside quotes, or quote symbol if inside"
  [quote line]
  ;; return nil if line is blank
  (when-not (s/blank? line)
    ;; get first char and rest of line
    (let [ch (first line)
          r (.substring line 1)]
      (cond
        ;; if parser is inside quotes, check if first char is same quote
        quote (if (= quote ch)
                ;; then go outside quotes...
                (recur nil r)
                ;; ... or stay in quotes
                (recur quote r))
        ;; if parser is outside quotes,
        ;; and it found quote, go inside :)
        (is-quote? ch) (recur ch r)
        ;; else check, maybe we found comment
        :else (if-let [comm (cut-cs line)]
                comm
                (recur nil r))))))

(defn find-comment-in-line
  [line]
  (find-comment-in-line* nil line))

;; Main functions

(defn find-comments-in-file 
  "Returns vector of comments."
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (->> rdr
         line-seq
         (map find-comment-in-line)
         ;; Remove all lines whithout comments
         (remove nil?)
         ;; Materialize our lazy seq before closing reader
         (into []))))



(defn fix-broken-slash
  "Replaces windows backslash with unix slash."
  [f]
  (s/replace f #"\\" "/"))

(defn find-php-files
  "Returns seq of php file paths."
  [root-path]
  (->> root-path
       ;; Let's make some java.io.File with our path,
       clojure.java.io/file
       ;; get recursively all files & directories,
       file-seq
       ;; and we need only files...
       (filter #(.isFile %))
       ;; ... which name ends with ".php"
       (filter #(-> % .getName (.endsWith ".php")))
       ;; and we need relative path to files
       (map #(.getPath %))
       ;; and fix windows slashes
       (map fix-broken-slash)
       ))
