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

;; Helpers

(defn is-quote?
  "Checks that character is a quotation symbol."
  [ch]
  (some #{ch} quotes-symbols))

(defn is-cs?
  "Checks that string is a CS."
  [s]
  (some #{s} comment-symbols))

(defn is-cs-beginning?
  "Checks that string is leading substring of one of CS."
  [s]
  (some #(.startsWith % s) comment-symbols))

;; Our state for reduce line:
;; index -- current index in line
;; quote -- nil or one of quotation symbols. If set to nil, parser is outside quotes, otherwise inside quotes.
;; cs -- part of comment symbol, used to parse CS of length more than one character.
;; comment-found -- true or false.

(defprotocol FsmState
  (step [this ch])
  (result [this]))

(declare ->InsideQuotes ->CommentBeginningFound)

(defrecord CommentFound [comment-part]
  FsmState
  (step [this ch]
    (update-in this [:comment-part] str ch))
  (result [this] comment-part))

(defrecord OutsideQuotes []
  FsmState
  (step [this ch]
    (cond
      ;; If input char is quote, change state to inside quotes
      (is-quote? ch) (->InsideQuotes ch)
      ;; If input char with current cs is CS, immideatly finish reduce
      ;; (god bless 'reduced' fn :) )
      (is-cs? (str ch)) (->CommentFound "")
      ;; If input char is leading char of one of CS, update cs
      (is-cs-beginning? (str ch)) (->CommentBeginningFound (str ch))
      ;; If nothing interesting, simply go to next character
      :else this))
  (result [this] nil))

(defrecord InsideQuotes [quote]
  FsmState
  (step [this ch]
    (if (= quote ch)
      (->OutsideQuotes)
      this))
  (result [this] nil))

(defrecord CommentBeginningFound [cs]
  FsmState
  (step [this ch]
    (let [s (str cs ch)]
      (cond
        ;; If input char is quote, change state to inside quotes
        (is-quote? ch) (->InsideQuotes ch)
        ;; If input char with current cs is CS, immideatly finish reduce
        ;; (god bless 'reduced' fn :) )
        (is-cs? s) (->CommentFound "")
        ;; If input char is leading char of one of CS, update cs
        (is-cs-beginning? s) (->CommentBeginningFound s)
        ;; If nothing interesting, simply go to next character
        :else (->OutsideQuotes))))
  (result [this] nil))

(defn find-comment-in-line
  [comment-symbols line]
  ;; Reduce line (seq of chars)
  (let [final-state (reduce step (->OutsideQuotes) (seq line))]
    ;; If comment found, return it
    (when-let [comment-part (result final-state)]
      comment-part)))

;; Main functions

(defn find-comments-in-file 
  "Returns vector of comments."
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (->> rdr
         line-seq
         ;; Here we implicitly using global symbol
         ;; 'comment-symbols', which is not good.
         ;; But in current task it's okay.
         ;; (also 'comment-symbols' used in helpers fns)
         (map (partial find-comment-in-line comment-symbols))
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
