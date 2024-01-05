# Gramatica

```
programa               -> void main { declaraciones instrucciones }

declaraciones          -> declaracion-variable declaraciones |
                          ε

declaracion-variable   -> tipo-variable declaracion-variable' ;

declaracion-variable'  -> [ num ] id |
                          lista-identificadores

tipo-variable          -> int |
                          float |
                          boolean

lista-identificadores  -> id asignacion-declaracion mas-identificadores

mas-identificadores    -> , id asignacion-declaracion mas-identificadores |
                          ε

asignacion-declaracion -> = expresion-logica |
                          ε

instrucciones          -> instruccion instrucciones |
                          ε

instruccion            -> declaracion-variable |
                          variable = expresion-logica ; |
                          if ( expresion-logica ) instruccion else-opcional |
                          while ( expresion-logica ) instruccion |
                          do instruccion while ( expresion-logica ) ; |
                          print ( variable ) ; |
                          { instrucciones }

else-opcional          -> else instruccion |
                          ε

variable               -> id array-opcional

array-opcional         -> [ expresion ] |
                          ε

expresion-logica       -> termino-logico expresion-logica'

expresion-logica'      -> || termino-logico expresion-logica' | 
                          ε

termino-logico         -> factor-logico termino-logico'

termino-logico'        -> && factor-logico termino-logico' |
                          ε

factor-logico          -> ! factor-logico | true | false | expresion-relacional

expresion-relacional   -> expresion oper-rel-opcional

oper-rel-opcional      -> operador-relacional expresion |
                          ε

operador-relacional    -> < | <= | > | >= | == | !=

expresion              -> termino expresion'

expresion'             -> + termino expresion' |
                          - termino expresion' |
                          ε

termino                -> factor termino'

termino'               -> * factor termino' |
                          / factor termino' |
                          % factor termino' |
                          ε

factor                 -> ( expresion ) |
                          variable |
                          num

```