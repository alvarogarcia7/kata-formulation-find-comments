(ns find-comments.core-test
  (:use midje.sweet
          find-comments.core))

(facts "canary tests"
  (fact "truthiness"
    true => true)
  
  (fact "falsiness"
  false => false))