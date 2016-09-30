(use srfi-13 srfi-1)


(define (find-duplicates l)
  (let loop ((l l) (seen '()) (r '()))
    (if (pair? l)
        (if (member (car l) seen)
            (if (member (car l) r)
                (loop (cdr l) seen r) ;; duplicate already listed
                (loop (cdr l) seen (cons (car l) r)))
            (loop (cdr l) (cons (car l) seen) r))
        r)))

(define layout (with-input-from-file "layout.scm" read))

(let ((dups (delete '() (find-duplicates (map cdr layout)))))
  (if (pair? dups)
      (error "duplicates in layout.scm" dups)))

(begin ;; helpers
  (define (wos s) (with-output-to-string (lambda () (write s))))
  (define (pad s n #!optional (padding #\space))
    (conc (make-string (max 0 (- n (string-length (conc s)))) padding) s)))

(print "// generated from gen-ThumboardLayout.java.scm and layout.scm
package com.adellica.thumbkeyboard;
import java.util.Arrays;
public class ThumboardLayout {
  private static boolean match(String [] p0, String [] p1) { return Arrays.equals(p0, p1); }
  /** returns string-representation of key as defined by KeyEvent KEYCODE_'s.
  */
  public static String parse(String [] p) {
    if(false) {}
")

(for-each
 (lambda (pair)
   (cond ((number? (car pair)) (print "    // unused " (wos (cdr pair))))
         ((not (null? (cdr pair)))
          (print
           "    else if(match(p, new String[]{
          "
           (string-join (map wos (cdr pair)) ",\n          ")
           "
    }))                                return " (wos (conc (car pair))) ";" ))))

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
                 (print (pad (car pair) 12) " " (cadr pair))
                 (for-each (lambda (s) (print "             " s)) (cddr pair))))
             layout))))
       ";
  }
}")

