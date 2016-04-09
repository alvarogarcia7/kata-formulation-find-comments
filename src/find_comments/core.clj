(ns find-comments.core
  (:require [clojure.string :as s]))

;; Small ubiquitous language dictionary:
;; -------------------------------------------------------------------------
;; | comment        |    | string for which we search
;; | line           |    | line from file, in which we are searching comment
;; | comment symbol | CS | string, after that in line starts comment
;; -------------------------------------------------------------------------

;; Main idea of how to find comment in line:
;; We have two different cases when comment exists.
;; 1) Simple case, when there is no bracketed CS.
;; 2) Not simple case, when there is one or more bracketed CS. 
;; At second case we need to apply some regex that will find comment.

;; So our algorithm will be:
;; 1) Check that line has at least one CS, else return nil
;; 2) Apply all regexs on line, if one of it returns comment, return it
;; 3) Find index of first CS in line, then substring line starting at this index.

(def comment-symbols ["#" "//"])


(defn is-quote?
  [ch]
  (some #{ch} [\' \"]))

(defn is-cs?
  [s]
  (some #{s} comment-symbols))

(defn is-cs-beginning?
  [s]
  (some #(.startsWith % s) comment-symbols))

(defrecord State [quotes comment-symbol comment])

(def start-state (->State #{} "" ""))

(defn step-quote
  [quotes ch]
  (let [q #{ch}]
    (if (some q quotes)
      (remove q quotes)
      (conj quotes ch))))

;; (reduce step start-state (seq "\"\""))

(defn step-cs
  [state s]
  state)

(defn step-else
  [state s]
  state)

(defn step 
  [state ch]
  (let [s (str ch)]
    (cond
      (is-quote? ch) (update-in state [:quotes] step-quote ch)
      (is-cs? s) (step-cs state s)
      :else (step-else state s))))

;; (reduce step () (seq ""))

;; (step (->Start) \#)

;; Remarks: current implementation doesn't care case when comment consists of many CS
;; So for example for line "test // test # hooray" we will return " hooray",
;; beacuse # CS was declared before // CS.

;; Regex part of solution

(defn create-regex
  "Creates regex to search case when there is one or more bracketed CS and trailing CS. 
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

(def cs-regexs (map create-regex comment-symbols))

(defn find-comment-using-regex
  "Applies regex to line and returns comment, if found."
  [regex line]
  ;; re-matches returns nil in case of nothing found,
  ;; so we using when-let to automatically return nil.
  
  ;; re-matches returns vector, where first element
  ;; is general match, and next elements is groups.
  ;; We need first group, so we need second element
  ;; in vector.
  (when-let [[_ comment] (re-matches regex line)]
    comment))

;; Split part of solution

(defn find-comment-using-split
  [comment-symbol line]
  ;; Take index of CS in line
  (let [index (.indexOf line comment-symbol)
        ;; And length of CS
        length (count comment-symbol)]
    ;; If index is negative, return nil
    (when-not (neg? index)
      ;; Otherwise, substring up to index
      ;; plus CS
      (.substring line (+ index length)))))

(defn apply-search-fn
  "Maps patterns to (f pattern line) and returns first not nil result.
  TODO: write tests!"
  [f patterns line]
  (->> patterns
       ;; Let's make a lazy seq 
       ;; of search results
       (map #(f % line))
       ;; Then drop all leading nil results...
       (drop-while nil?)
       ;; ... and take first (not nil)
       first))

(defn ensure-at-least-one-cs-in-line
  [comment-symbols line]
  (->> comment-symbols
       ;; For each CS get it index in line
       (map #(.indexOf line %))
       ;; Some not negative,
       ;; pos? doesn't work cause of 
       ;; (= (pos? 0) false)
       (some (complement neg?))))

(defn find-comment-in-line
  [comment-symbols comment-regexs line]
  ;; 1) Check that line has at least one CS, else return nil
  (when (ensure-at-least-one-cs-in-line comment-symbols line)
    ;; 2) Apply all regexs on line, if one of it returns comment, return it
    (if-let [by-regex (apply-search-fn find-comment-using-regex comment-regexs line)]
      by-regex
      ;; 3) Find index of first CS in line, then substring line starting at this index.
      (apply-search-fn find-comment-using-split comment-symbols line))))

;; (ensure-at-least-one-cs-in-line ["#" "//"] "//  ")
;; (find-comment-using-split "#" "//  ")
;; (find-comment-in-line ["#" "//"] cs-regexs "//  ")

;; Main functions

(defn find-comments-in-file 
  "Returns vector of comments."
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (->> rdr
         line-seq
         (map (partial find-comment-in-line comment-symbols cs-regexs))
         (remove nil?)
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
