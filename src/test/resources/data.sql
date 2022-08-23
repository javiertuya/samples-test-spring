--Hasta Spring boot 2.6.7, incluir spring.datasource.data= en application-test.properties
--suponia que el fichero data.sql de src/main/resources no se ejecutaba.
--Pero tras 2.7.* siempre se ejecuta, lo que causa que los tests no empiecen con una bd limpia
--Tampoco funciona asignar un nombre de fichero no existente a spring.datasource.data
--ni otras soluciones recomendadas que se han buscado
--Para mantener el fichero data.sql cuando se ejecuta el servidor (para usar como demo)
--se opta por crear este fichero en src/test/resources que elimina los datos y deja la bd limpia para test
delete from cliente
