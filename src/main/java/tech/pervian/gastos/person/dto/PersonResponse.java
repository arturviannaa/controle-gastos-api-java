package tech.pervian.gastos.person.dto;

import tech.pervian.gastos.person.Person;

public record PersonResponse(Long id, String name, Integer age) {

    public static PersonResponse de(Person person) {
        return new PersonResponse(person.getId(), person.getName(), person.getAge());
    }
}
