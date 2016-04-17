;; A version from an imperative programmer:)

;; I was in pair with @sherpc on a meetup and
;; this version was inspired by his FSM version, which seemed a bit complicated

;; It borrows heavily on files analysis part of his version, but in string parsing (find-comment-in-line) is genuine

;; I am still not happy yet with "let" in both cases: to make "if-let" jump to "false" condition upon a "nil" I
;; had to use "seq", but in the next line to get the value I had to unwrap the sequence back...

;; And sure I am not happy that Clojure binds "recur" to a function implementation (with the fixed number of
;; parameters) and not to a function definition, thus I have to use suboptimal function calls where simple recur
;; would do.

;; @sherpc use nullable argument to make "recur" work, but it requires a helper function and IMO is not nice.

;; Can not see any reason why Clojure reader can not recur to an overloaded function.


(ns find-comments.core
  (:gen-class))

(defn find-comments-in-file [filename]
  "Return vector of comments"
    (with-open [rdr (clojure.java.io/reader filename)]
      (->> rdr
           line-seq
           (map find-comment-in-line)
           (remove nil?)
           (into []))))

(defn find-php-files [root-path]
  "Returns seq of php file paths"
  (->> root-path
    java.io.File.
    file-seq
    (filter #(.isFile %))
    (filter #(-> % .getName
                 (.endsWith ".php")))
    (map #(.getPath %))))


(def comment-s '("#" "//"))
(def quote-s '("'" "\""))

(defn comment-s? [s]
  (filter #(.startsWith s %) comment-s))

(defn quote-s? [s]
   (filter #(.startsWith s %) quote-s))

(defn find-comment-in-line
  "We implement a FSM via an overloaded function -- when inside a quote, pass two arguments, including opening quote symb, otherwise just one (string itself). Don't know yet hot to recur in an overloaded function, but cant see any reason why Clojure reader does not recur in there"
    ([s] (when (pos? (count s))
      (if-let [cs (seq (comment-s? s))] 
          (subs s (count (first cs))) ; yes, a comment symbol found, just return the remainder of a string
          (if-let [qs (seq (quote-s? s))] ; no, lets check for an opening quote
              (find-comment-in-line (subs s 1) qs) ; yes, an opening quote found, now go look for an end quote
              (find-comment-in-line (subs s 1)))))) ; no, just some other symbol found, check for the rest
     ([s q] (when (pos? (count s))
       (if-let [qs (seq (quote-s? s))] ; lets check if it is a quote
         (if (= qs q) ; is it a closing quote?
           (get-comment (subs s 1)) ; yes, lets check for the rest
           (get-comment (subs s 1) q)))))) ; no, just ignore the symbol, continue looking for a closing quote
 
