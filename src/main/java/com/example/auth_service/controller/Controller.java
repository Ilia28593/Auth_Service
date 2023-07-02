package com.example.auth_service.controller;

import com.example.auth_service.api.Person;
import com.example.auth_service.config.JwtAuthentication;
import com.example.auth_service.config.Role;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class Controller {
    private final AuthService authService;

    private final PersonService personService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("hello/user")
    public ResponseEntity<String> helloUser() {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        return ResponseEntity.ok("Hello user " + authInfo.getPrincipal() + "!");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("hello/admin")
    public ResponseEntity<String> helloAdmin() {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        log.info("{}", authInfo);
        return ResponseEntity.ok("Hello admin " + authInfo.getPrincipal() + "!");
    }

    /**
     * Контроллер отвечающий за создание пользователя.
     *
     * @param request - передается по http в теле запроса.
     * @return возвращает пользователя или если обьек не найден BadRequest.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public Person crate(@RequestBody Person request) {
        return personService.create(request);
    }

    /**
     * Контроллер отвечающий за получения списка всех пользователей.
     *
     * @return возвращает псписок всех пользователей.
     */
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/all")
    public List<Person> findAll() {
        return personService.getAll();
    }

    /**
     * Контроллер отвечающий за получения пользователя по id.
     *
     * @param id - передается по http в заголовке запроса.
     * @return возвращает пользователя или если обьек не найден BadRequest.
     */
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("{id}")
    public Person getBuId(@PathVariable long id) {
        return personService.getUserById(id);
    }

    /**
     * Контроллер отвечающий за удаление пользователя.
     *
     * @param id - передается по http в теле запроса.
     * @return возвращает сообщение об удаление или если обьек не найден BadRequest.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        return new ResponseEntity<>(personService.removeById(String.valueOf(id)), HttpStatus.OK);
    }

    /**
     * Контроллер отвечающий за обнавление пользователя.
     *
     * @param id - передается по http в теле запроса.
     * @return возвращает пользователя или если обьек не найден BadRequest.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("{id}")
    public Person update(@PathVariable long id, @RequestBody Person request) {
        return personService.update(id, request);
    }

    /**
     * Контроллер отвечающий за с обнавление роли пользователя и получение его.
     *
     * @param id   - передается по http в заголовке запроса.
     * @param role - передается по http в заголовке запроса.
     * @return возвращает пользователя или если обьек не найден BadRequest.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("{id}/role")
    public Person updateRole(@PathVariable long id, @RequestParam(name = "role") String role) {
        return personService.updateRole(id, Role.valueOf(role));
    }
}