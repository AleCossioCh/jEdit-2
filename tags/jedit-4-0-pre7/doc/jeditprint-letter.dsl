<!DOCTYPE style-sheet PUBLIC "-//James Clark//DTD DSSSL Style Sheet//EN" [
<!ENTITY dbstyle PUBLIC "-//Norman Walsh//DOCUMENT DocBook Print Stylesheet//EN"
CDATA DSSSL> ]>

<style-sheet>
<style-specification use="print">
<style-specification-body>

(define %two-side% #t)
(define %section-autolabel% #t)
(define %paper-type% "USletter")
(define %admon-graphics% #f)

(define %body-start-indent% 2pi)
(define %block-start-indent% 1pi)

;(define %default-quadding% 'justify)
;(define %hyphenation% #t)

(declare-characteristic preserve-sdata?
	"UNREGISTERED::James Clark//Characteristic::preserve-sdata?" #f)

(define %visual-acuity% "presbyopic")


(element funcsynopsis (process-children))

(element void (empty-sosofo))

(define %funcsynopsis-style% 'ansi)
(element void (literal "();"))

(element funcprototype
  (let ((paramdefs (select-elements (children (current-node))
                                    (normalize "paramdef"))))
    (make sequence
      (make paragraph
        start-indent: (if (equal? (gi (parent)) (normalize "listitem"))
                          0
                          (inherited-start-indent))
        font-family-name: %mono-font-family%
        (process-children))
      (if (equal? %funcsynopsis-style% 'kr)
          (with-mode kr-funcsynopsis-mode
            (process-node-list paramdefs))
          (empty-sosofo)))))

(define %verbatim-size-factor% 0.85)

(element (listitem abstract) (process-children))

</style-specification-body>
</style-specification>
<external-specification id="print" document="dbstyle">
</style-sheet>
