(ns find-comments.core
  (:require [clojure.string :as s]))

;; Small ubiquitous language dictionary:
;; -------------------------------------------------------------------------
;; | comment        |    | string for which we search
;; | line           |    | line from file, in which we are searching comment
;; | comment symbol | CS | string, after that in line starts comment
;; -------------------------------------------------------------------------

;; Main idea of how to find comment in line:
;; Reduce line as seq of chars. Reduce state is a record which implements FsmState protocol.
;; See finite state machine scheme in fsm-schema.png file.

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

(defprotocol FsmState
  "Describes finite state machine state. 
  We can go to next state ('step') and get result from state (comment or nil)."
  (step [this ch])
  (result [this]))

(declare ->InsideQuotes ->CommentBeginningFound)

;; The most simple state.
(defrecord CommentFound [comment-part]
  FsmState
  ;; Simply concat chars to comment-part
  (step [this ch]
    (update-in this [:comment-part] str ch))
  ;; Return comment-part
  (result [this] comment-part))

;; 'Main' state.
(defrecord OutsideQuotes []
  FsmState
  (step [this ch]
    (cond
      (is-quote? ch) (->InsideQuotes ch)
      (is-cs? (str ch)) (->CommentFound "")
      (is-cs-beginning? (str ch)) (->CommentBeginningFound (str ch))
      :else this))
  (result [this] nil))

(defrecord InsideQuotes [quote]
  FsmState
  (step [this ch]
    ;; If input char is equal to our quote, go back outside.
    ;; Otherwise ignore all :)
    (if (= quote ch)
      (->OutsideQuotes)
      this))
  (result [this] nil))

;; Very similar to OutsideQuotes
(defrecord CommentBeginningFound [cs]
  FsmState
  (step [this ch]
    ;; Let concat current leading CS substring with next input char
    (let [s (str cs ch)]
      (cond
        ;; forget about DRY for next two lines of code :)
        (is-quote? ch) (->InsideQuotes ch)
        (is-cs? s) (->CommentFound "")
        (is-cs-beginning? s) (->CommentBeginningFound s)
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
