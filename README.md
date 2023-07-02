# Auth_Service

* Микросервис отвечающий За доступ к микросервестному приложениючерез систему генераци JWT Token

* Является точкой входа приложение



* **/
* @PreAuthorize("hasAuthority('ADMIN')")
* @PostMapping
* public Person crate(@RequestBody Person request) {
* return personService.create(request);
* **/

* Формирует Уровень доступа(уровень прав доступа) в приложение по системе (Admin/User)

* Для отладки подключен Swagger http://localhost:8082/swagger-ui.html#/controller
* ![](../../Desktop/2023-07-02_23-29-57.png)

* 
* 
* 
* 
* Формирование HTTP запросов к другому микросервису для получение информации из БД и Создание и Обновление информации
* 
![](../../Desktop/2023-07-02_23-32-38.png)

