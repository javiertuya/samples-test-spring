Informe de descuento de clientes desde base de datos (problema 3b y 3c)

!-- Muestra como se pueden utilizar parámetros de datos tabulares 
!-- https://jbehave.org/reference/stable/tabular-parameters.html
!-- (p.e. para representar los datos iniciales en la base de datos y las salidas)
!-- En un caso general la inicialización de la base de datos se realizaría en el mapeo de pasos a Java
!-- (no se hace aqui para ilustrar la escritura de datos a BD partiendo de la tabla)

Narrative:
In order to ver descuentos aplicables a clientes 
As a empleado del banco
I want to visualizar un informe con los descuentos aplicables a los clientes (Problema 3b) 
filtrando por la edad del cliente (Problema 3c)
					 
Scenario:  Informe de todos los clientes con algun descuento
Given los clientes en base de datos:
|id|edad|nuevo|cupon|tarjeta|
|1|18|S|N|N|
|2|38|S|S|N|
|3|21|S|N|S|
|4|25|N|N|N|
|5|40|N|S|N|
|6|42|N|N|S|
|7|39|N|S|S|
When ver informe descuentos clientes de edad cualquiera
Then los descuentos son:
|id|descuento|
|1|15|
|2|20|
|5|20|
|6|10|
|7|30|
When ver informe descuentos clientes de edad 40
Then los descuentos son:
|id|descuento|
|5|20|
|6|10|
