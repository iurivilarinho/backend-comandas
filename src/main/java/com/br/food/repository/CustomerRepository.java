package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
