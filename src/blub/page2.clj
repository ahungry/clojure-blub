(use 'hiccup.core)

(defn input [name]
  (html [:label {:class (str name "-label")}
    [:input {:name name :type "text"}]]))

(def pinput (comp println input))

<!DOCTYPE html>
<html>
  <head>
    <title>You can freely mix html and hiccup just fine.</title>
  </head>
  <body>
    <form name='my-form'>
      (println (format "Hello %s, thanks for visiting!" (:name request)))
      (pinput "username")
      (pinput "password")
    </form name='my-form'>
  </body>
</html>
