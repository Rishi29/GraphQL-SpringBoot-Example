package com.graphql.dao;

import org.springframework.data.repository.CrudRepository;

import com.graphql.entity.Person;

public interface PersonRepository  extends CrudRepository<Person, Integer>{

	Person findByEmail(String email);

}
