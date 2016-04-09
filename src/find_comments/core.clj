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

;; Some rules:
;; 1) Quotes and CS can't be same
;; 2) One CS can't be substring of another

(def comment-symbols ["#" "//"])
(def quotes-symbols [\' \"])

(defn is-quote?
  [ch]
  (some #{ch} quotes-symbols))

(defn is-cs?
  [s]
  (some #{s} comment-symbols))

(defn is-cs-beginning?
  [s]
  (some #(.startsWith % s) comment-symbols))

(defrecord State [index quote cs comment-found])

(def start-state (->State 0 nil nil false))

(defn step-quote 
  [quote ch]
  ;; when-not seems more unclear than if
  ;; in current context
  (if (= quote ch) 
    nil 
    quote))

(defn step 
  [{:keys [index quote cs] :as state} ch]
  (let [s (str ch)
        next-state (update-in state [:index] inc)]
    (cond
      quote (update-in next-state [:quote] step-quote ch)
      (is-quote? ch) (assoc next-state :quote ch)
      (or (is-cs? s) (is-cs? (str cs s))) (reduced (assoc state :comment-found true))
      (is-cs-beginning? (str cs s)) (assoc-in next-state [:cs] (str cs s))
      :else next-state)))

(reduce step start-state (seq "//1"))

;; (step (->Start) \#)

;; Remarks: current implementation doesn't care case when comment consists of many CS
;; So for example for line "test // test # hooray" we will return " hooray",
;; beacuse # CS was declared before // CS.

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
  [comment-symbols line]
  ;; 1) Check that line has at least one CS, else return nil
  (when (ensure-at-least-one-cs-in-line comment-symbols line)
    (let [{:keys [index comment-found]} (reduce step start-state (seq line))]
      (when comment-found
        (.substring line (inc index))))))

;; Main functions

(defn find-comments-in-file 
  "Returns vector of comments."
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (->> rdr
         line-seq
         (map (partial find-comment-in-line comment-symbols))
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
