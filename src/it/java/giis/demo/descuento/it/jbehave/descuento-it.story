Informe de descuento de clientes desde base de datos con ui (problema 3d)
					
!-- Este es un caso donde debe haber varios pasos When/Then porque lo que se está probando es precisamente
!-- la secuencia de pasos y el efecto de los cambios en el interfaz de usuario
!-- Se ilustra tambien en este ejemplo como se pueden implementar los mapeos de los pasos para 
!-- realizar prueba de un interfaz de usuario (en este caso Swing)
 
Narrative:
In order to ver descuentos aplicables a clientes 
As a empleado del banco
I want to añadir un interfaz de usuari al informe de descuentos (Problema 3d)

Scenario: Filtro por edad en informe desde ui de descuentos aplicables a clientes
Given los siguientes clientes en base de datos:
|id|edad|nuevo|cupon|tarjeta|
|1|18|S|N|N|
|2|38|S|S|N|
|3|21|S|N|S|
|4|25|N|N|N|
|5|40|N|S|N|
|6|42|N|N|S|
|7|39|N|S|S|
!-- paso 1
When Se inicia la ventana
Then los descuentos visualizados son:
|Id|% Descuento|
|1|15|
|2|20|
|5|20|
|6|10|
|7|30|
!-- paso 2
When se cambia la edad a 40
Then los descuentos visualizados son:
|Id|% Descuento|
|5|20|
|6|10|
!-- paso 3
When se cambia la edad a 39
Then los descuentos visualizados son:
|Id|% Descuento|
|5|20|
|6|10|
|7|30|
!-- paso 4
When se elimina la edad
Then los descuentos visualizados son:
|Id|% Descuento|
|1|15|
|2|20|
|5|20|
|6|10|
|7|30|
