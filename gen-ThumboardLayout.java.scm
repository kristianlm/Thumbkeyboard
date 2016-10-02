(use srfi-13 srfi-1 matchable)

;; like rassoc but return multiple values
(define (rassoc* k l #!optional (tst eq?) (extract car))
  (let loop ((l l)
             (r '()))
    (if (pair? l)
        (if (tst k (extract (car l)))
            (loop (cdr l) (cons (car l) r))
            (loop (cdr l) r))
        r)))

(define layout (with-input-from-file "layout.scm" read))

(let ((dups (filter (o pair? cdr)
                    (map (lambda (pair) (map car (rassoc* (cdr pair) layout equal? cdr))) layout))))
  (if (pair? dups)
      (error "duplicates in layout.scm" dups)))

(begin ;; helpers
  (define (wos s) (with-output-to-string (lambda () (write s))))
  (define (pad s n #!optional (padding #\space))
    (conc (make-string (max 0 (- n (string-length (conc s)))) padding) s)))

(define (spec->regex spec)

  (define (fmt s)
    (match s
      ((? string? s) (conc (string-translate* s '(("." . "\\."))) "\\n"))
      (('+ p ...) (conc "(" (fmt `(: ,@p)) ")+"))
      ((': p ...) (conc (string-join (map fmt p) "")))
      ('any ".")
      (('* p ...) (conc "(" (string-join (map fmt p)) ")*"))
      (else (error "dont know what to do with " s))))

  ;; only on-the-fly chords don't have an implicit "... ..." match at
  ;; the end.
  (define simple? (string? (car spec)))

  (conc "(?s)" ;; http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#DOTALL
        (string-join (map fmt spec) "")
        (if simple?
            (fmt "... ...")
            "")))

(print "// generated from gen-ThumboardLayout.java.scm and layout.scm
package com.adellica.thumbkeyboard;
public class ThumboardLayout {
  /** returns string-representation of key as defined by KeyEvent KEYCODE_'s.
  */
  public static String parse(String p) {
    if(false) {}
")

(for-each
 (lambda (pair)
   (cond ((number? (car pair)) (print "    // unused " (wos (cdr pair))))
         ((not (null? (cdr pair)))
          (print
           "    else if(p.matches(" (wos (spec->regex (cdr pair)))
           " )) return "
           (wos (match (car pair)
                  ((? symbol? s) (conc "key " s))
                  ((? string? s) (conc "input " s))
                  ('(repeat) (conc "repeat"))))
           ";" ))))

 layout)

(print "    return null;
  }
")

(print "  public static String help() {
    return "
       (wos
        (with-output-to-string
          (lambda ()
            (for-each
             (lambda (pair)
               (when (not (number? (car pair)))
                 (print (pad (car pair) 12) " " (and (pair? (cdr pair)) (cadr pair)))
                 (for-each (lambda (s) (print "             " s)) (or (and (pair? (cdr pair)) (cddr pair)) '()))))
             layout))))
       ";
  }
}")

