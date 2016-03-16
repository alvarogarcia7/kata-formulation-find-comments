(ns find-comments.core-test
  (:use midje.sweet
          find-comments.core))

(facts "canary tests"
  (fact "truthiness"
    true => true)
  
  (fact "falsiness"
  false => false))

(facts "about finding comments"
  (fact "multiple comments in a file"
    (find-comments-in-file "./dev-resources/sample_code/file1.php") => '(
      "comment (single line)" 
      " Помогите!! Я в ловушке на этом языке. Можете ли вы перевести это?"
      " another comment")))