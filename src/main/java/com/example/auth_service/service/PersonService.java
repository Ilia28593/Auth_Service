package com.example.auth_service.service;

import com.example.auth_service.api.Person;
import com.example.auth_service.config.Role;
import com.example.auth_service.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int DELAY_MILLIS = 1000;
    private final WebClient webClient;

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису для получения пользователя по email.
     *
     * @param email - передается по http в заголовке запроса.
     * @return возвращает пользователя или если обьек не найден BadRequest.
     */
    public Person getUserByEmail(final String email) {
        log.info("Request Person from getUserByEmail");
        Person person = webClient.get()
                .uri("/api/email/{id}", email)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (ClientResponse clientResponse) -> {
                    log.error("Incorrect request getUserByEmail");
                    throw new ValidationException("Email is incorrect write");
                })
                .bodyToMono(Person.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block();
        log.info("Confirm person from getUserByEmail");
        return person;
    }

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису для получения пользователя по id.
     *
     * @param id - передается по http в заголовке запроса.
     * @return возвращает пользователя или BadRequest.
     */
    public Person getUserById(final long id) {
        log.info("Request Person from getUserById");
        Person person = webClient.get()
                .uri("api/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (ClientResponse clientResponse) -> {
                    log.error("User from id {} is no exist", id);
                    throw new ValidationException(String.format("User from id %s is no exist", id));
                })
                .bodyToMono(Person.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block();
        log.info("Confirm Person from getUserById");
        return person;
    }

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису на создани пользователя и получение его.
     *
     * @param entity - передается по http в заголовке запроса.
     * @return возвращает пользователя или BadRequest.
     */
    public Person create(Person entity) {
        log.info("Request  create Person from {} {}", entity.getLastName(), entity.getFirstName());
        checkValidEmail(entity.getEmail());
        validCreatePerson(entity);
        Person person = webClient.post()
                .uri("api")
                .bodyValue(entity)
                .retrieve()
                .onStatus(HttpStatus::isError, (ClientResponse clientResponse) -> {
                    log.error("Problem from create user {} {}", entity.getLastName(), entity.getFirstName());
                    throw new ValidationException(String.format("Problem from create user %s %s",
                            entity.getLastName(), entity.getFirstName()));
                })
                .bodyToMono(Person.class)
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block();
        log.info("Completed create Person from {} {}", entity.getLastName(), entity.getFirstName());
        return person;
    }

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису на обнавление пользователя и получение его.
     *
     * @param entity - передается по http в заголовке запроса.
     * @return возвращает пользователя или BadRequest.
     */
    public Person update(long id, Person entity) {
        log.info("Request  update Person from {} {}", entity.getLastName(), entity.getFirstName());
        entity.setId(id);
        Person person = webClient.put()
                .uri("api")
                .bodyValue(createUpdatePerson(entity))
                .retrieve()
                .onStatus(HttpStatus::isError, (ClientResponse clientResponse) -> {
                    log.error("Problem from update user {} {}", entity.getLastName(), entity.getFirstName());
                    throw new ValidationException(String.format("Problem from update user %s %s",
                            entity.getLastName(), entity.getFirstName()));
                })
                .bodyToMono(Person.class)
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block();
        log.info("Completed update Person from {} {}", entity.getLastName(), entity.getFirstName());
        return person;
    }

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису на обнавление роли пользователя и получение его.
     *
     * @param id   - передается по http в заголовке запроса.
     * @param role - передается по http в заголовке запроса.
     * @return возвращает пользователя или BadRequest.
     */
    public Person updateRole(long id, Role role) {
        log.info("Request Person from updateRole");
        Person person = webClient.put()
                .uri("api/{id}/change/{role}", id, role)
                .retrieve()
                .onStatus(HttpStatus::isError, (ClientResponse clientResponse) -> {
                    log.error("Problem from updateRole user {}", role);
                    throw new ValidationException(String.format("Problem from updateRole %s", role));
                })
                .bodyToMono(Person.class)
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block();
        log.info("Confirm Person from updateRole");
        return person;
    }

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису на удаление пользователя.
     *
     * @param id - передается по http в заголовке запроса.
     * @return возвращает сообщение или BadRequest.
     */
    public String removeById(final String id) {
        log.info("Request Person from removeById");
        String info = webClient.delete()
                .uri("api/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::isError, (ClientResponse clientResponse) -> {
                    log.error("Problem from removeById");
                    throw new ValidationException("Problem from removeById ");
                })
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block();
        log.info("Confirm Person from removeById");
        return info;
    }

    /**
     * Метод отвечающий за создание HTTP запроса к микросервису на список всех пользователей.
     *
     * @return возвращает список всех пользователей или BadRequest.
     */
    public List<Person> getAll() {
        log.info("Request Person from all List Users");
        List<Person> people = Objects.requireNonNull(webClient.get()
                .uri("api/all")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Person>>() {
                })
                .retryWhen(Retry.fixedDelay(MAX_RETRY_ATTEMPTS, Duration.ofMillis(DELAY_MILLIS)))
                .block()).stream().toList();
        log.info("Confirm Person from all List Users");
        return people;
    }

    private void checkValidEmail(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            log.error("User email invalid {}", email);
            throw new ValidationException(String.format("User email invalid %s", email));
        }
    }

    private void validCreatePerson(Person entity) {
        if (entity.getFirstName().isBlank()) {
            throw new ValidationException("User FirstName is  blank");
        } else if (entity.getLastName().isBlank()) {
            throw new ValidationException("User LastName is blank");
        } else if (entity.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("User Birthday is future");
        } else if (entity.getPassword().isBlank()) {
            throw new ValidationException("User password is blank");
        }
    }

    private Person createUpdatePerson(Person entity) {
        Person updatePerson = getUserById(entity.getId());
        updatePerson.setEmail(
                entity.getEmail().isBlank() ?
                        updatePerson.getEmail() :
                        entity.getEmail());
        updatePerson.setBirthday(
                entity.getBirthday().isAfter(LocalDate.now()) ?
                        updatePerson.getBirthday() :
                        entity.getBirthday());
        updatePerson.setFirstName(
                entity.getFirstName().isBlank() ?
                        updatePerson.getFirstName() :
                        entity.getFirstName());
        updatePerson.setLastName(
                entity.getLastName().isBlank() ?
                        updatePerson.getLastName() :
                        entity.getLastName());
        updatePerson.setPassword(
                entity.getPassword().isBlank() ?
                        updatePerson.getPassword() :
                        entity.getPassword());
        return updatePerson;
    }
}
