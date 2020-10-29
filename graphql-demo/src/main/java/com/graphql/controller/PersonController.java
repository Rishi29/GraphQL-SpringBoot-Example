package com.graphql.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.graphql.dao.PersonRepository;
import com.graphql.entity.Person;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;




@RestController
public class PersonController {

	@Autowired
	private PersonRepository personRepository; 
	
	@Value("classpath:person.graphqls")
	private Resource schemaResource;
	
	private GraphQL graphQL;
	
	@PostConstruct
	public void loadSchema() throws IOException {
		File schemaFile = schemaResource.getFile();
		
		TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);
		RuntimeWiring wiring = buildWiring();
		GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
		graphQL = GraphQL.newGraphQL(schema).build();
		
	}
	
	private RuntimeWiring buildWiring() {
		
		DataFetcher<List<Person>> fetcher1 = data->{
			return (List<Person>) personRepository.findAll();
		};
		
		DataFetcher<Person> fetcher2 = data->{
			return personRepository.findByEmail(data.getArgument("email"));
		};
		
		
		return RuntimeWiring.newRuntimeWiring().type("Query",typeWriting->
			typeWriting.dataFetcher("getAllPerson", fetcher1).dataFetcher("findPerson", fetcher2)).build();
		
	}

	
	
	@PostMapping("/getAll")
	public ResponseEntity<Object> getAll(@RequestBody String query){
		
		ExecutionResult result = graphQL.execute(query);
		return new ResponseEntity<Object>(result, HttpStatus.OK);
		
	}
	
	@PostMapping("/getPersonByEmail")
	public ResponseEntity<Object> getPersonByEmail(@RequestBody String query){
		
		ExecutionResult result = graphQL.execute(query);
		return new ResponseEntity<Object>(result, HttpStatus.OK);
		
	}
	
	
	/*General CRUD operation*/	
	
	@PostMapping("/addPerson")
	public Person addPerson(@RequestBody Person person) {
		personRepository.save(person);
		return  person;
	}
	@PostMapping("/addPersons")
	public String addPerson(@RequestBody List<Person>persons) {
		personRepository.saveAll(persons);
		return "record inserted "+ persons.size();
	}
	
	@GetMapping("/person/{id}")
	public Person getPerson(@PathVariable Integer id) {
		Optional<Person> person  = personRepository.findById(id);
		
		if(person.isPresent()) 
			return person.get();
		return null;
	}
	
	@GetMapping("/findAllPerson")
	public List<Person> getPerson(){
		
		List<Person> persons = (List<Person>)personRepository.findAll();
		//return (List<Person>)personRepository.findAll();
		
		return persons;
	}
}
