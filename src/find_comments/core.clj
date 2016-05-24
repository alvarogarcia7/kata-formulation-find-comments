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

(defn find-comment-in-line
  [line]
  'fix-me)

;; Main functions

(defn find-comments-in-file 
  "Returns vector of comments."
  [filename]
  'fix-me)


(defn find-php-files
  "Returns seq of php file paths."
  [root-path]
  'fix-me)
